package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.chat.AllianceMessageChannel;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.chat.AllianceMessageChannelImpl;
import io.github.aquerr.eaglefactions.common.messaging.chat.FactionMessageChannelImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;
import org.spongepowered.api.util.Tristate;

import java.util.*;

public class ChatMessageListener extends AbstractListener
{
    private final ChatConfig chatConfig;

    public ChatMessageListener(EagleFactions plugin)
    {
        super(plugin);
        this.chatConfig = plugin.getConfiguration().getChatConfig();
    }

    @Listener
    @IsCancelled(Tristate.FALSE)
    public void onChatMessage(final MessageChannelEvent.Chat event, final @Root Player player)
    {
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(!optionalPlayerFaction.isPresent())
        {
            if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
            {
                MessageChannel messageChannel = event.getOriginalChannel();
                final Collection<MessageReceiver> chatMembers = messageChannel.getMembers();
                Set<MessageReceiver> newReceivers = new HashSet<>(chatMembers);
                for(MessageReceiver messageReceiver : chatMembers)
                {
                    if(messageReceiver instanceof Player)
                    {
                        final Player receiver = (Player) messageReceiver;
                        if(EagleFactionsPlugin.CHAT_LIST.containsKey(receiver.getUniqueId()) && EagleFactionsPlugin.CHAT_LIST.get(receiver.getUniqueId()) != ChatEnum.GLOBAL)
                        {
                            newReceivers.remove(receiver);
                        }
                    }
                }
                messageChannel = MessageChannel.fixed(newReceivers);
                event.setChannel(messageChannel);
            }

            if(!this.chatConfig.getNonFactionPlayerPrefix().toPlain().equals(""))
            {
                final Text.Builder formattedMessage = Text.builder();
                formattedMessage.append(this.chatConfig.getFactionStartPrefix())
                        .append(this.chatConfig.getNonFactionPlayerPrefix())
                        .append(this.chatConfig.getFactionEndPrefix())
                        .append(event.getMessage());
                event.setMessage(formattedMessage);
            }

            return;
        }

        MessageChannel messageChannel = event.getOriginalChannel();
        final Faction playerFaction = optionalPlayerFaction.get();

        final Text.Builder formattedMessage = Text.builder();

        final Text.Builder factionAndRankPrefix = Text.builder();
        final Text.Builder factionPrefixText = Text.builder();
        final Text.Builder rankPrefixText = Text.builder();
        final Text.Builder chatTypePrefix = Text.builder();
        final Text.Builder message = Text.builder();

        //Message = Prefixes + Player NAME + Text
        //OriginalMessage = Player NAME + Text
        //RawMessage = Text

        switch(EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()))
        {
            case FACTION:
            {
                message.append(Text.of(TextColors.GREEN, event.getRawMessage()));
                chatTypePrefix.append(getFactionChatPrefix());
                messageChannel = new FactionMessageChannelImpl(playerFaction);
                final MutableMessageChannel channel = messageChannel.asMutable();
                channel.addMember(Sponge.getServer().getConsole());
                getAdmins().forEach(channel::addMember);
                break;
            }
            case ALLIANCE:
            {
                message.append(Text.of(TextColors.BLUE, event.getRawMessage()));
                chatTypePrefix.append(getAllianceChatPrefix());
                messageChannel = new AllianceMessageChannelImpl(playerFaction);
                final MutableMessageChannel channel = messageChannel.asMutable();
                channel.addMember(Sponge.getServer().getConsole());
                getAdmins().forEach(channel::addMember);
                break;
            }
            case GLOBAL:
            default:
                //If player is chatting in global chat then directly get raw message from event.
                message.append(event.getFormatter().getBody().format());

                //Suppress message for other factions if someone is in the faction's chat.
                if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
                {
                    final Collection<MessageReceiver> chatMembers = messageChannel.getMembers();
                    final Set<MessageReceiver> newReceivers = new HashSet<>(chatMembers);
                    for(final MessageReceiver messageReceiver : chatMembers)
                    {
                        if (!(messageReceiver instanceof Player))
                            continue;

                        final Player receiver = (Player) messageReceiver;
                        if (!EagleFactionsPlugin.CHAT_LIST.containsKey(receiver.getUniqueId()))
                            continue;

                        if (EagleFactionsPlugin.CHAT_LIST.get(receiver.getUniqueId()) == ChatEnum.GLOBAL)
                            continue;

                        final Optional<Faction> receiverFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(receiver.getUniqueId());
                        if (!receiverFaction.isPresent())
                            continue;

                        if(playerFaction.getAlliances().contains(receiverFaction.get().getName()))
                        {
                            continue;
                        }
                        else if(!receiverFaction.get().getName().equals(playerFaction.getName()))
                        {
                            newReceivers.remove(receiver);
                        }
                    }
                    messageChannel = MessageChannel.fixed(newReceivers);
                }
        }

        factionPrefixText.append(getFactionPrefix(playerFaction));

        if(this.chatConfig.shouldDisplayRank())
        {
            rankPrefixText.append(getRankPrefix(playerFaction, player));
        }

        if (this.chatConfig.isFactionPrefixFirstInChat())
        {
            factionAndRankPrefix.append(factionPrefixText.build());
            factionAndRankPrefix.append(rankPrefixText.build());
        }
        else
        {
            factionAndRankPrefix.append(rankPrefixText.build());
            factionAndRankPrefix.append(factionPrefixText.build());
        }

        formattedMessage.append(message.build());
        final MessageEvent.MessageFormatter messageFormatter = event.getFormatter();
        messageFormatter.getHeader().insert(0, new SimpleTextTemplateApplier(TextTemplate.of(factionAndRankPrefix)));
        messageFormatter.getHeader().insert(0, new SimpleTextTemplateApplier(TextTemplate.of(chatTypePrefix)));
        messageFormatter.setBody(formattedMessage);
        event.setChannel(messageChannel);
    }

    private Text getFactionPrefix(final Faction playerFaction)
    {
        //Get faction prefix from Eagle Factions.
        if(this.chatConfig.getChatPrefixType().equals("tag"))
        {
            if(!playerFaction.getTag().toPlainSingle().equals(""))
            {
                if (this.chatConfig.canColorTags())
                {
                    //Get faction's tag
                    return Text.builder()
                            //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                            .append(this.chatConfig.getFactionStartPrefix(), playerFaction.getTag(), this.chatConfig.getFactionEndPrefix())
                            .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                            .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                            .build();
                }
                else
                {
                    //Get faction's tag
                    return Text.builder()
                            //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                            .append(this.chatConfig.getFactionStartPrefix(), Text.of(TextColors.GREEN, playerFaction.getTag()), this.chatConfig.getFactionEndPrefix())
                            .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                            .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                            .build();
                }
            }
        }
        else if (this.chatConfig.getChatPrefixType().equals("name"))
        {
            //Add faction name
            return Text.builder()
                    .append(this.chatConfig.getFactionStartPrefix(), Text.of(TextColors.GREEN, playerFaction.getName(), TextColors.RESET), this.chatConfig.getFactionEndPrefix())
                    .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                    .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                    .build();
        }
        return Text.of("");
    }

//    private List<MessageReceiver> filterReceivers(MessageChannelEvent.Chat event, Player player, )
//    {
//        Set<MessageReceiver> receivers = new HashSet<>();
//        Text.Builder chatTypePrefix = Text.builder();
//
//        if (EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()).equals(ChatEnum.ALLIANCE))
//        {
//            message.append(Text.of(TextColors.BLUE, event.getRawMessage()));
//            chatTypePrefix.append(getAlliancePrefix());
//            messageChannel.asMutable().clearMembers();
//
//            for (String allianceName : playerFaction.getAlliances())
//            {
//                Faction allyFaction = super.getPlugin().getFactionLogic().getFactionByName(allianceName);
//                if(allyFaction != null)
//                    receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(allyFaction));
//            }
//            receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(playerFaction));
//        }
//        else if (EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()).equals(ChatEnum.FACTION))
//        {
//            message.append(Text.of(TextColors.GREEN, event.getRawMessage()));
//            chatTypePrefix.append(getFactionPrefix());
//            messageChannel.asMutable().clearMembers();
//            receivers = new HashSet<>(getPlugin().getFactionLogic().getOnlinePlayers(playerFaction));
//        }
//
//        //Add users with factions-admin mode to the collection. Admins should see all chats.
//        for(final UUID adminUUID : EagleFactionsPlugin.ADMIN_MODE_PLAYERS)
//        {
//            final Optional<Player> optionalAdminPlayer = Sponge.getServer().getPlayer(adminUUID);
//            if(optionalAdminPlayer.isPresent())
//            {
//                receivers.add(optionalAdminPlayer.get());
//            }
//        }
//
//        messageChannel = MessageChannel.fixed(receivers);
//
//        //Add chatType to formattedMessage
//        formattedMessage.append(chatTypePrefix.build());
//    }

    private Text getAllianceChatPrefix()
    {
        return Text.builder()
                .append(this.chatConfig.getFactionStartPrefix(), Text.of(TextColors.BLUE, Messages.ALLIANCE_CHAT, TextColors.RESET), this.chatConfig.getFactionEndPrefix())
                .build();
    }

    private Text getFactionChatPrefix()
    {
        return Text.builder()
                .append(this.chatConfig.getFactionStartPrefix(), Text.of(TextColors.GREEN, Messages.FACTION_CHAT, TextColors.RESET), this.chatConfig.getFactionEndPrefix())
                .build();
    }

    private Text getRankPrefix(final Faction faction, final Player player)
    {
        //Get leader prefix.
        if(faction.getLeader().equals(player.getUniqueId()))
        {
            return Text.builder()
                    .append(Text.of(this.chatConfig.getFactionStartPrefix(), TextColors.GOLD, Messages.LEADER, TextColors.RESET, this.chatConfig.getFactionEndPrefix()))
                    .build();
        }
        //Get officer prefix.
        else if(faction.getOfficers().contains(player.getUniqueId()))
        {
            return Text.builder()
                    .append(Text.of(this.chatConfig.getFactionStartPrefix(), TextColors.GOLD, Messages.OFFICER, TextColors.RESET, this.chatConfig.getFactionEndPrefix()))
                    .build();
        }
        //Get recruit prefix.
        else if(faction.getRecruits().contains(player.getUniqueId()))
        {
            return Text.builder()
                    .append(Text.of(this.chatConfig.getFactionStartPrefix(), TextColors.GOLD, Messages.RECRUIT, TextColors.RESET, this.chatConfig.getFactionEndPrefix()))
                    .build();
        }
        return Text.of("");
    }

    private List<MessageReceiver> getAdmins()
    {
        final List<MessageReceiver> admins = new ArrayList<>();
        //Add users with factions-admin mode to the collection. Admins should see all chats.
        for(final UUID adminUUID : EagleFactionsPlugin.ADMIN_MODE_PLAYERS)
        {
            final Optional<Player> optionalAdminPlayer = Sponge.getServer().getPlayer(adminUUID);
            optionalAdminPlayer.ifPresent(admins::add);
        }
        return admins;
    }
}
