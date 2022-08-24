package io.github.aquerr.eaglefactions.messaging.chat;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class ChatMessageHelper
{
    public static TextComponent getFactionPrefix(Faction faction)
    {
        final ChatConfig chatConfig =  EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig();

        //Get faction prefix from Eagle Factions.
        if(chatConfig.getChatPrefixType().equals("tag"))
        {
            return getChatTagPrefix(faction);
        }
        else if (chatConfig.getChatPrefixType().equals("name"))
        {
            return getChatFactionNamePrefix(faction);
        }
        else
        {
            return Component.empty();
        }
    }

    public static TextComponent getChatTagPrefix(Faction faction)
    {
        final ChatConfig chatConfig =  EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig();

        TextComponent factionTag = faction.getTag();
        if (!chatConfig.canColorTags())
            factionTag = factionTag.toBuilder().color(chatConfig.getDefaultTagColor()).build();

        return Component.text()
                .append(chatConfig.getFactionStartPrefix(), factionTag, chatConfig.getFactionEndPrefix())
                .hoverEvent(HoverEvent.showText(Component.text("Click to view information about the faction!", BLUE)))
                .clickEvent(ClickEvent.runCommand("/f info " + faction.getName()))
                .build();
    }

    public static TextComponent getChatFactionNamePrefix(Faction faction)
    {
        final ChatConfig chatConfig =  EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig();

        return Component.text()
                .append(chatConfig.getFactionStartPrefix(), Component.text(faction.getName(), GREEN), chatConfig.getFactionEndPrefix())
                .hoverEvent(HoverEvent.showText(Component.text("Click to view information about the faction!", BLUE)))
                .clickEvent(ClickEvent.runCommand("/f info " + faction.getName()))
                .build();
    }

    public static TextComponent getChatPrefix(ServerPlayer player)
    {
        final TextComponent.Builder chatTypePrefix = Component.text();

        ChatEnum chatType = EagleFactionsPlugin.CHAT_LIST.get(player.uniqueId());
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

    private static TextComponent getAllianceChatPrefix()
    {
        return Component.text()
                .append(EFMessageService.getInstance().resolveComponentWithMessage("chat.alliance.prefix"))
                .build();
    }

    private static TextComponent getFactionChatPrefix()
    {
        return Component.text()
                .append(EFMessageService.getInstance().resolveComponentWithMessage("chat.faction.prefix"))
                .build();
    }

    public static TextComponent getRankPrefix(final ChatEnum chatType, final Faction faction, final ServerPlayer player)
    {
        final ChatConfig chatConfig =  EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig();

        if(faction.getLeader().equals(player.uniqueId()))
        {
            if (!chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.LEADER))
                return null;

            return Component.text()
                    .append(EFMessageService.getInstance().resolveComponentWithMessage("chat.leader.prefix"))
                    .build();
        }
        else if(faction.getOfficers().contains(player.uniqueId()))
        {
            if (!chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.OFFICER))
                return null;

            return Component.text()
                    .append(EFMessageService.getInstance().resolveComponentWithMessage("chat.officer.prefix"))
                    .build();
        }
        else if (faction.getMembers().contains(player.uniqueId()))
        {
            if (!chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.MEMBER))
                return null;

            return Component.text()
                    .append(EFMessageService.getInstance().resolveComponentWithMessage("chat.member.prefix"))
                    .build();
        }
        else
        {
            if(!chatConfig.getVisibleRanks().get(chatType).contains(FactionMemberType.RECRUIT))
                return null;

            return Component.text()
                    .append(EFMessageService.getInstance().resolveComponentWithMessage("chat.recruit.prefix"))
                    .build();
        }
    }

    public static Audience removeFactionChatPlayersFromAudience(final Audience audience)
    {
        return audience.filterAudience(audience1 ->
        {
            if (!(audience1 instanceof ServerPlayer))
                return true;

            ServerPlayer serverPlayer = (ServerPlayer) audience1;
            if (EagleFactionsPlugin.CHAT_LIST.containsKey(serverPlayer.uniqueId())
                && EagleFactionsPlugin.CHAT_LIST.get(serverPlayer.uniqueId()) != ChatEnum.GLOBAL)
            {
                return false;
            }
            return true;
        });
    }


}
