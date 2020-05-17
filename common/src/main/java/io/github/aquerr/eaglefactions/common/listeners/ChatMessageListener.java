package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
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
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
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
        MessageChannel messageChannel = event.getOriginalChannel();
        final MessageEvent.MessageFormatter messageFormatter = event.getFormatter();

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(!optionalPlayerFaction.isPresent())
        {
            //Suppress message for other people
            if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
            {
                final MessageChannel suppressedChannel = hideMessageForPlayersInFactionChat(messageChannel);
                event.setChannel(suppressedChannel);
            }

            //Add non-faction prefix tag.
            if(!this.chatConfig.getNonFactionPlayerPrefix().toPlain().equals(""))
            {
                //TODO: Try to use placeholder instead of having "start" and "end" prefix.
                messageFormatter.getHeader().insert(0, new SimpleTextTemplateApplier(TextTemplate.of(this.chatConfig.getNonFactionPlayerPrefix())));
            }
            return;
        }

        //Code below is for players that have a faction.
        final Faction playerFaction = optionalPlayerFaction.get();
        final Text.Builder formattedMessage = Text.builder();
        final Text.Builder factionAndRankPrefix = Text.builder();
        final Text.Builder factionPrefix = Text.builder();
        final Text.Builder rankPrefix = Text.builder();
        final Text.Builder chatTypePrefix = Text.builder();
        final Text.Builder message = Text.builder();

        ChatEnum chatType = EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId());

        //Message = Prefixes + Player NAME + Text
        //OriginalMessage = Player NAME + Text
        //RawMessage = Text

        if(chatType == null)
            chatType = ChatEnum.GLOBAL;

        switch(chatType)
        {
            case FACTION:
            {
                message.append(Text.of(TextColors.GREEN, messageFormatter.getBody().format()));
                chatTypePrefix.append(getFactionChatPrefix());
                messageChannel = new FactionMessageChannelImpl(playerFaction);
                final MutableMessageChannel channel = messageChannel.asMutable();
                channel.addMember(Sponge.getServer().getConsole());
                getAdminReceivers().forEach(channel::addMember);
                break;
            }
            case ALLIANCE:
            {
                message.append(Text.of(TextColors.BLUE, messageFormatter.getBody().format()));
                chatTypePrefix.append(getAllianceChatPrefix());
                messageChannel = new AllianceMessageChannelImpl(playerFaction);
                final MutableMessageChannel channel = messageChannel.asMutable();
                channel.addMember(Sponge.getServer().getConsole());
                getAdminReceivers().forEach(channel::addMember);
                break;
            }
            case GLOBAL:
            default:
                //If player is chatting in global chat then directly get raw message from event.
                message.append(messageFormatter.getBody().format());

                //Suppress message for other factions if someone is in the faction's chat.
                if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
                {
                    messageChannel = hideMessageForPlayersInFactionChat(messageChannel);
                }
        }

        factionPrefix.append(getFactionPrefix(playerFaction));

        final Text prefix = getRankPrefix(chatType, playerFaction, player);
        if (prefix != null)
            rankPrefix.append(prefix);

        if (this.chatConfig.isFactionPrefixFirstInChat())
        {
            factionAndRankPrefix.append(factionPrefix.build());
            factionAndRankPrefix.append(rankPrefix.build());
        }
        else
        {
            factionAndRankPrefix.append(rankPrefix.build());
            factionAndRankPrefix.append(factionPrefix.build());
        }

        formattedMessage.append(message.build());
        messageFormatter.getHeader().insert(0, new SimpleTextTemplateApplier(TextTemplate.of(factionAndRankPrefix)));
        messageFormatter.getHeader().insert(0, new SimpleTextTemplateApplier(TextTemplate.of(chatTypePrefix)));
        messageFormatter.setBody(formattedMessage);
        event.setChannel(messageChannel);
    }

    private MessageChannel hideMessageForPlayersInFactionChat(final MessageChannel messageChannel)
    {
        final Collection<MessageReceiver> chatMembers = messageChannel.getMembers();
        final Set<MessageReceiver> newReceivers = new HashSet<>(chatMembers);
        for(final MessageReceiver messageReceiver : chatMembers)
        {
            if(!(messageReceiver instanceof Player))
                continue;

            final Player receiver = (Player) messageReceiver;

            if(EagleFactionsPlugin.CHAT_LIST.containsKey(receiver.getUniqueId()) && EagleFactionsPlugin.CHAT_LIST.get(receiver.getUniqueId()) != ChatEnum.GLOBAL)
            {
                newReceivers.remove(receiver);
            }
        }
        return MessageChannel.fixed(newReceivers);
    }

    private Text getFactionPrefix(final Faction playerFaction)
    {
        //Get faction prefix from Eagle Factions.
        if(this.chatConfig.getChatPrefixType().equals("tag"))
        {
            if(playerFaction.getTag().toPlain().equals(""))
                return playerFaction.getTag();

            Text factionTag = playerFaction.getTag();
            if (!this.chatConfig.canColorTags())
                factionTag = factionTag.toBuilder().color(TextColors.GREEN).build();

            return Text.builder()
                    .append(this.chatConfig.getFactionStartPrefix(), factionTag, this.chatConfig.getFactionEndPrefix())
                    .onHover(TextActions.showText(Text.of(TextColors.BLUE, "Click to view information about the faction!")))
                    .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                    .build();
        }
        else if (this.chatConfig.getChatPrefixType().equals("name"))
        {
            return Text.builder()
                    .append(this.chatConfig.getFactionStartPrefix(), Text.of(TextColors.GREEN, playerFaction.getName(), TextColors.RESET), this.chatConfig.getFactionEndPrefix())
                    .onHover(TextActions.showText(Text.of(TextColors.BLUE, "Click to view information about the faction!")))
                    .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                    .build();
        }
        return playerFaction.getTag();
    }

    private Text getAllianceChatPrefix()
    {
        return Text.builder()
                .append(TextSerializers.FORMATTING_CODE.deserialize(Messages.ALLIANCE_CHAT_PREFIX))
                .build();
    }

    private Text getFactionChatPrefix()
    {
        return Text.builder()
                .append(TextSerializers.FORMATTING_CODE.deserialize(Messages.FACTION_CHAT_PREFIX))
                .build();
    }

    private Text getRankPrefix(final ChatEnum chatType, final Faction faction, final Player player)
    {
        if(faction.getLeader().equals(player.getUniqueId()))
        {
            if (!this.chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.LEADER))
                return null;

            return Text.builder()
                    .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Messages.LEADER_PREFIX)))
                    .build();
        }
        else if(faction.getOfficers().contains(player.getUniqueId()))
        {
            if (!this.chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.OFFICER))
                return null;

            return Text.builder()
                    .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Messages.OFFICER_PREFIX)))
                    .build();
        }
        else if (faction.getMembers().contains(player.getUniqueId()))
        {
            if (!this.chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.MEMBER))
                return null;

            return Text.builder()
                    .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Messages.MEMBER_PREFIX)))
                    .build();
        }
        else
        {
            if(!this.chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.RECRUIT))
                return null;

            return Text.builder()
                    .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Messages.RECRUIT_PREFIX)))
                    .build();
        }
    }

    private List<MessageReceiver> getAdminReceivers()
    {
        final List<MessageReceiver> admins = new ArrayList<>();
        for(final UUID adminUUID : super.getPlugin().getPlayerManager().getAdminModePlayers())
        {
            final Optional<Player> optionalAdminPlayer = Sponge.getServer().getPlayer(adminUUID);
            optionalAdminPlayer.ifPresent(admins::add);
        }
        return admins;
    }
}
