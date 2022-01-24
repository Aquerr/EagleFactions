package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.messaging.chat.AllianceAudienceImpl;
import io.github.aquerr.eaglefactions.messaging.chat.ChatMessageHelper;
import io.github.aquerr.eaglefactions.messaging.chat.FactionAudienceImpl;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.entity.living.player.PlayerChatFormatter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.util.Tristate;

import java.util.Optional;

import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

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
    public void onChatMessage(final PlayerChatEvent event, final @Root ServerPlayer player)
    {
        Audience audience = event.audience().orElse(event.originalAudience());
        final PlayerChatFormatter playerChatFormatter = event.chatFormatter().orElse(event.originalChatFormatter());
        Component message = event.message();

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if(!optionalPlayerFaction.isPresent())
        {
            //Suppress message for other people
            if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
            {
                final Audience suppressedAudience = ChatMessageHelper.removeFactionChatPlayersFromAudience(audience);
                event.setAudience(suppressedAudience);
            }

            //Add non-faction prefix tag.
            if(!PlainTextComponentSerializer.plainText().serialize(this.chatConfig.getNonFactionPlayerPrefix()).equals(""))
            {
                event.setMessage(this.chatConfig.getNonFactionPlayerPrefix().append(message));
            }
            return;
        }

        //Code below is for players that have a faction.
        final Faction playerFaction = optionalPlayerFaction.get();
        final TextComponent.Builder factionAndRankPrefix = Component.text();
        final TextComponent.Builder factionPrefix = Component.text();
        final TextComponent.Builder rankPrefix = Component.text();
        final TextComponent.Builder chatTypePrefix = Component.text();
        final TextComponent.Builder finalMessage = Component.text();

        ChatEnum chatType = Optional.ofNullable(EagleFactionsPlugin.CHAT_LIST.get(player.uniqueId()))
                .orElse(ChatEnum.GLOBAL);

        //Message = Prefixes + Player NAME + Text
        //OriginalMessage = Player NAME + Text
        //RawMessage = Text

        chatTypePrefix.append(ChatMessageHelper.getChatPrefix(player));

        switch(chatType)
        {
            case FACTION:
            {
                finalMessage.append(message.color(GREEN));
                audience = FactionAudienceImpl.forFaction(playerFaction);
                break;
            }
            case ALLIANCE:
            {
                finalMessage.append(message.color(BLUE));
                audience = AllianceAudienceImpl.forFaction(playerFaction);
                break;
            }
            case GLOBAL:
            default:
                //If player is chatting in global chat then directly get raw message from event.
                finalMessage.append(message);

                //Suppress message for other factions if someone is in the faction's chat.
                if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
                {
                    audience = ChatMessageHelper.removeFactionChatPlayersFromAudience(audience);
                }
        }

        final TextComponent fPrefix = ChatMessageHelper.getFactionPrefix(playerFaction);
        factionPrefix.append(fPrefix);

        final TextComponent rPrefix = ChatMessageHelper.getRankPrefix(chatType, playerFaction, player);
        if (rPrefix != null)
            rankPrefix.append(rPrefix);

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

        event.setAudience(audience);
        event.setMessage(factionAndRankPrefix.append(chatTypePrefix).append(finalMessage).build());
    }
}
