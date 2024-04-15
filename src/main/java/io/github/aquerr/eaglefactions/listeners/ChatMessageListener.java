package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.messaging.chat.ChatMessageHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.util.Tristate;

import java.util.Optional;
import java.util.function.Predicate;

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
    public void onChatMessageDecorate(final PlayerChatEvent.Decorate event, @Root ServerPlayer player)
    {
        ChatEnum chatType = Optional.ofNullable(EagleFactionsPlugin.CHAT_LIST.get(player.uniqueId()))
                .orElse(ChatEnum.GLOBAL);
        modifyMessage(event, chatType);
    }

    @Listener
    @IsCancelled(Tristate.FALSE)
    public void onChatMessageSubmit(final PlayerChatEvent.Submit event, @Root ServerPlayer player)
    {
        // filter who will receive the message
        final Faction playerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId())
                .orElse(null);

        ChatEnum chatType = Optional.ofNullable(EagleFactionsPlugin.CHAT_LIST.get(player.uniqueId()))
                .orElse(ChatEnum.GLOBAL);

        modifyMessageVisibility(event, playerFaction, chatType);

        modifySender(event, player);
    }

    private void modifyMessage(PlayerChatEvent.Decorate event,
                               ChatEnum senderChatType)
    {
        Component message = event.message();
        switch(senderChatType)
        {
            case FACTION:
            {
                event.setMessage(message.color(GREEN));
                break;
            }
            case ALLIANCE:
            {
                event.setMessage(message.color(BLUE));
                break;
            }
        }
    }

    private void modifySender(PlayerChatEvent.Submit event,
                              ServerPlayer sender)
    {
        final Faction playerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(sender.uniqueId())
                .orElse(null);
        if (playerFaction == null)
        {
            //Add non-faction prefix tag.
            if(!PlainTextComponentSerializer.plainText().serialize(this.chatConfig.getNonFactionPlayerPrefix()).isEmpty())
            {
                event.setMessage(LinearComponents.linear(this.chatConfig.getNonFactionPlayerPrefix(), Component.text("<" + sender.name() + "> "), event.message()));
                return;
            }
        }

        final TextComponent.Builder factionAndRankPrefix = Component.text();
        final TextComponent.Builder factionPrefix = Component.text();
        final TextComponent.Builder rankPrefix = Component.text();
        final TextComponent.Builder chatTypePrefix = Component.text();

        chatTypePrefix.append(ChatMessageHelper.getChatPrefix(sender));

        final TextComponent fPrefix = ChatMessageHelper.getFactionPrefix(playerFaction);
        factionPrefix.append(fPrefix);

        final TextComponent rPrefix = ChatMessageHelper.getRankPrefix(playerFaction, sender.uniqueId());
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

        event.setSender(LinearComponents.linear(chatTypePrefix, factionAndRankPrefix, event.sender()));
    }

    private void modifyMessageVisibility(PlayerChatEvent.Submit event,
                                         Faction senderFaction,
                                         ChatEnum senderChatType)
    {
        Predicate<ServerPlayer> visibilityPredicate =
                        isSenderFactionPlayerPredicate(senderFaction)
                                .or(isSenderChatChannelPredicate(senderChatType, senderFaction))
                                .or(isAdminReciverPredicate())
                                .and(event.filter().orElse((serverPlayer -> true)));

        event.setFilter(visibilityPredicate);
    }

    private Predicate<ServerPlayer> isSenderFactionPlayerPredicate(Faction senderFaction) {
        return receiver -> senderFaction != null && senderFaction.containsPlayer(receiver.uniqueId());
    }

    private Predicate<ServerPlayer> isSenderChatChannelPredicate(ChatEnum senderChatType, Faction senderFaction) {
        return receiver -> {

            if (senderChatType == ChatEnum.ALLIANCE)
            {
                Faction receiverFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(receiver.uniqueId()).orElse(null);
                return receiverFaction != null && senderFaction.isAlly(receiverFaction);
            }
            else if (senderChatType == ChatEnum.GLOBAL)
            {
                if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
                {
                    ChatEnum receiverChatType = Optional.ofNullable(EagleFactionsPlugin.CHAT_LIST.get(receiver.uniqueId()))
                            .orElse(ChatEnum.GLOBAL);

                    return receiverChatType == ChatEnum.GLOBAL;
                }
                return true;
            }
            return false;
        };
    }

    private Predicate<? super ServerPlayer> isAdminReciverPredicate()
    {
        return receiver -> EagleFactionsPlugin.getPlugin().getPlayerManager().getAdminModePlayers().stream()
                .anyMatch(x -> x.uniqueId().equals(receiver.uniqueId()));
    }
}
