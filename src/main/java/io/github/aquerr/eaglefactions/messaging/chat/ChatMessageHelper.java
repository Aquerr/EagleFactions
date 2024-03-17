package io.github.aquerr.eaglefactions.messaging.chat;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public final class ChatMessageHelper
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

        return text()
                .append(chatConfig.getFactionStartPrefix(), factionTag, chatConfig.getFactionEndPrefix())
                .hoverEvent(HoverEvent.showText(text("Click to view information about the faction!", BLUE)))
                .clickEvent(ClickEvent.runCommand("/f info " + faction.getName()))
                .build();
    }

    public static TextComponent getChatFactionNamePrefix(Faction faction)
    {
        final ChatConfig chatConfig =  EagleFactionsPlugin.getPlugin().getConfiguration().getChatConfig();

        return text()
                .append(chatConfig.getFactionStartPrefix(), text(faction.getName(), GREEN), chatConfig.getFactionEndPrefix())
                .hoverEvent(HoverEvent.showText(text("Click to view information about the faction!", BLUE)))
                .clickEvent(ClickEvent.runCommand("/f info " + faction.getName()))
                .build();
    }

    public static TextComponent getChatPrefix(ServerPlayer player)
    {
        final TextComponent.Builder chatTypePrefix = text();

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
        return text()
                .append(EFMessageService.getInstance().resolveComponentWithMessage("chat.alliance.prefix"))
                .build();
    }

    private static TextComponent getFactionChatPrefix()
    {
        return text()
                .append(EFMessageService.getInstance().resolveComponentWithMessage("chat.faction.prefix"))
                .build();
    }

    public static TextComponent getRankPrefix(final Faction faction, final UUID playerUUID)
    {
        Rank rank = RankManagerImpl.getHighestRank(faction.getPlayerRanks(playerUUID));
        if (rank != null && rank.canDisplayInChat())
            return text()
                    .append(text("["))
                    .append(LegacyComponentSerializer.legacyAmpersand().deserialize(rank.getDisplayName()))
                    .append(text("]"))
                    .build();

        return null;
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

    public static Set<? extends Audience> getAdminReceivers()
    {
        return EagleFactionsPlugin.getPlugin().getPlayerManager().getAdminModePlayers();
    }

    private ChatMessageHelper()
    {

    }
}
