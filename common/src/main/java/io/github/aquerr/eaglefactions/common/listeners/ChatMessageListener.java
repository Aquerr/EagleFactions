package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.*;

public class ChatMessageListener extends AbstractListener
{
    private final ConfigFields configFields;

    public ChatMessageListener(EagleFactions plugin)
    {
        super(plugin);
        this.configFields = plugin.getConfiguration().getConfigFields();
    }

    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player)
    {
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(!optionalPlayerFaction.isPresent())
        {
            if(this.configFields.shouldSupressOtherFactionsMessagesWhileInTeamChat())
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
//                            final Optional<Faction> receiverFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(receiver.getUniqueId());
//                            if(receiverFaction.isPresent())
//                            {
                                newReceivers.remove(receiver);
//                            }
                        }
                    }
                }
                messageChannel = MessageChannel.fixed(newReceivers);
                event.setChannel(messageChannel);
            }
            return;
        }

        MessageChannel messageChannel = event.getOriginalChannel();
        Faction playerFaction = optionalPlayerFaction.get();

        Text.Builder formattedMessage = Text.builder();

        Text.Builder factionAndRankPrefix = Text.builder();
        Text.Builder otherPrefixesAndPlayer = Text.builder();
        Text.Builder factionPrefixText = Text.builder();
        Text.Builder rankPrefixText = Text.builder();
        Text.Builder message = Text.builder();

        //Message = Prefixes + Player NAME + Text
        //OriginalMessage = Player NAME + Text
        //RawMessage = Text

        //Get Other Plugin Prefixes and Nickname from message.
        otherPrefixesAndPlayer.append(event.getMessage().getChildren().get(0));

        //Get ChatType from Eagle Faction and add it to the formattedMessage
        if (EagleFactionsPlugin.CHAT_LIST.containsKey(player.getUniqueId()))
        {
            Set<MessageReceiver> receivers = new HashSet<>();
            Text.Builder chatTypePrefix = Text.builder();

            if (EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()).equals(ChatEnum.ALLIANCE))
            {
                message.append(Text.of(TextColors.BLUE, event.getRawMessage()));
                chatTypePrefix.append(getAlliancePrefix());
                messageChannel.asMutable().clearMembers();

                for (String allianceName : playerFaction.getAlliances())
                {
                    Faction allyFaction = super.getPlugin().getFactionLogic().getFactionByName(allianceName);
                    if(allyFaction != null)
                        receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(allyFaction));
                }
                receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(playerFaction));
            }
            else if (EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()).equals(ChatEnum.FACTION))
            {
                message.append(Text.of(TextColors.GREEN, event.getRawMessage()));
                chatTypePrefix.append(getFactionPrefix());
                messageChannel.asMutable().clearMembers();
                receivers = new HashSet<>(getPlugin().getFactionLogic().getOnlinePlayers(playerFaction));
            }

            //Add users with factions-admin mode to the collection. Admins should see all chats.
            for(final UUID adminUUID : EagleFactionsPlugin.ADMIN_MODE_PLAYERS)
            {
                final Optional<Player> optionalAdminPlayer = Sponge.getServer().getPlayer(adminUUID);
                if(optionalAdminPlayer.isPresent())
                {
                    receivers.add(optionalAdminPlayer.get());
                }
            }

            receivers.add(Sponge.getServer().getConsole());
            messageChannel = MessageChannel.fixed(receivers);

            //Add chatType to formattedMessage
            formattedMessage.append(chatTypePrefix.build());
        }
        else
        {
            //If player is chatting in global chat then directly get raw message from event.
            message.append(event.getMessage().getChildren().get(1));

            //Suppress message for other factions if someone is in the faction's chat.
            if(this.configFields.shouldSupressOtherFactionsMessagesWhileInTeamChat())
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

        //Get faction prefix from Eagle Factions.
        if(getPlugin().getConfiguration().getConfigFields().getChatPrefixType().equals("tag"))
        {
            if(!playerFaction.getTag().toPlainSingle().equals(""))
            {
                if (getPlugin().getConfiguration().getConfigFields().canColorTags())
                {
                    //Get faction's tag
                    Text factionTag = Text.builder()
                            //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                            .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), playerFaction.getTag(), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                            .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                            .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                            .build();

                    factionPrefixText.append(factionTag);
                }
                else
                {
                    //Get faction's tag
                    Text factionTag = Text.builder()
                            //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                            .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.GREEN, playerFaction.getTag()), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                            .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                            .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                            .build();

                    factionPrefixText.append(factionTag);
                }
            }
        }
        else if (getPlugin().getConfiguration().getConfigFields().getChatPrefixType().equals("name"))
        {
            //Add faction name
            Text factionNamePrefix = Text.builder()
                    .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.GREEN, playerFaction.getName(), TextColors.RESET), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                    .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                    .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                    .build();

            factionPrefixText.append(factionNamePrefix);
        }

        if(getPlugin().getConfiguration().getConfigFields().shouldDisplayRank())
        {
            //Get leader prefix.
            if(playerFaction.getLeader().equals(player.getUniqueId()))
            {
                Text leaderPrefix = Text.builder()
                        .append(Text.of(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), TextColors.GOLD, PluginMessages.LEADER, TextColors.RESET, getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix()))
                        .build();

                rankPrefixText.append(leaderPrefix);
            }
            //Get officer prefix.
            else if(playerFaction.getOfficers().contains(player.getUniqueId()))
            {
                Text officerPrefix = Text.builder()
                        .append(Text.of(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), TextColors.GOLD, PluginMessages.OFFICER, TextColors.RESET, getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix()))
                        .build();

                rankPrefixText.append(officerPrefix);
            }
            //Get recruit prefix.
            else if(playerFaction.getRecruits().contains(player.getUniqueId()))
            {
                Text recruitPrefix = Text.builder()
                        .append(Text.of(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), TextColors.GOLD, PluginMessages.RECRUIT, TextColors.RESET, getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix()))
                        .build();

                rankPrefixText.append(recruitPrefix);
            }
        }

        if (getPlugin().getConfiguration().getConfigFields().isFactionPrefixFirstInChat())
        {
            factionAndRankPrefix.append(factionPrefixText.build());
            factionAndRankPrefix.append(rankPrefixText.build());
        }
        else
        {
            factionAndRankPrefix.append(rankPrefixText.build());
            factionAndRankPrefix.append(factionPrefixText.build());
        }

        //Add faction tag and faction rank
        formattedMessage.append(factionAndRankPrefix.build());
        //Add Other Plugins Prefixes
        formattedMessage.append(otherPrefixesAndPlayer.build());
        //Add player name
        //formattedMessage.append(playerText.build());
        //Add message
        formattedMessage.append(message.build());

        //Build message & print it.
        Text messageToPrint = Text.builder()
                .append(formattedMessage.build())
                .build();

        event.setChannel(messageChannel);
        event.setMessage(messageToPrint);
        return;
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

    private Text getAlliancePrefix()
    {
        return Text.builder()
                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.BLUE, PluginMessages.ALLIANCE_CHAT, TextColors.RESET), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                .build();
    }

    private Text getFactionPrefix()
    {
        return Text.builder()
                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.GREEN, PluginMessages.FACTION_CHAT, TextColors.RESET), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                .build();
    }
}
