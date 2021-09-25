package io.github.aquerr.eaglefactions.common.messaging.chat;

import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ChatMessageHelper
{
    public static Text getFactionPrefix(Faction faction)
    {
        final ChatConfig chatConfig =  EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig();

        //Get faction prefix from Eagle Factions.
        if(chatConfig.getChatPrefixType().equals("tag"))
        {
            //TODO: Remove this.. as all factions are required to have a tag.
            if(faction.getTag().toPlain().equals(""))
                return faction.getTag();

            Text factionTag = faction.getTag();
            if (!chatConfig.canColorTags())
                factionTag = factionTag.toBuilder().color(TextColors.GREEN).build();

            return Text.builder()
                    .append(chatConfig.getFactionStartPrefix(), factionTag, chatConfig.getFactionEndPrefix())
                    .onHover(TextActions.showText(Text.of(TextColors.BLUE, "Click to view information about the faction!")))
                    .onClick(TextActions.runCommand("/f info " + faction.getName()))
                    .build();
        }
        else if (chatConfig.getChatPrefixType().equals("name"))
        {
            return Text.builder()
                    .append(chatConfig.getFactionStartPrefix(), Text.of(TextColors.GREEN, faction.getName(), TextColors.RESET), chatConfig.getFactionEndPrefix())
                    .onHover(TextActions.showText(Text.of(TextColors.BLUE, "Click to view information about the faction!")))
                    .onClick(TextActions.runCommand("/f info " + faction.getName()))
                    .build();
        }
        else
        {
            return null;
        }
    }

    public static Text getChatPrefix(Player player)
    {
        final Text.Builder chatTypePrefix = Text.builder();

        ChatEnum chatType = EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId());
        if(chatType == null)
            chatType = ChatEnum.GLOBAL;

        switch(chatType)
        {
            case FACTION:
            {
                chatTypePrefix.append(getFactionChatPrefix());
                break;

            }
            case ALLIANCE:
            {
                chatTypePrefix.append(getAllianceChatPrefix());
                break;
            }
        }
        return chatTypePrefix.build();
    }

    private static Text getAllianceChatPrefix()
    {
        return Text.builder()
                .append(TextSerializers.FORMATTING_CODE.deserialize(Messages.ALLIANCE_CHAT_PREFIX))
                .build();
    }

    private static Text getFactionChatPrefix()
    {
        return Text.builder()
                .append(TextSerializers.FORMATTING_CODE.deserialize(Messages.FACTION_CHAT_PREFIX))
                .build();
    }

    public static Text getRankPrefix(final ChatEnum chatType, final Faction faction, final Player player)
    {
        final ChatConfig chatConfig =  EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig();

        if(faction.getLeader().equals(player.getUniqueId()))
        {
            if (!chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.LEADER))
                return null;

            return Text.builder()
                    .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Messages.LEADER_PREFIX)))
                    .build();
        }
        else if(faction.getOfficers().contains(player.getUniqueId()))
        {
            if (!chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.OFFICER))
                return null;

            return Text.builder()
                    .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Messages.OFFICER_PREFIX)))
                    .build();
        }
        else if (faction.getMembers().contains(player.getUniqueId()))
        {
            if (!chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.MEMBER))
                return null;

            return Text.builder()
                    .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Messages.MEMBER_PREFIX)))
                    .build();
        }
        else
        {
            if(!chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.RECRUIT))
                return null;

            return Text.builder()
                    .append(Text.of(TextSerializers.FORMATTING_CODE.deserialize(Messages.RECRUIT_PREFIX)))
                    .build();
        }
    }

    public static MessageChannel removeFactionChatPlayersFromChannel(final MessageChannel messageChannel)
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


}
