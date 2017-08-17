package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;

public class ChatMessageListener
{
    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player)
    {
        if(FactionLogic.getFactionName(player.getUniqueId()) != null)
        {
            String message = event.getRawMessage().toPlain();

            String factionTag = FactionLogic.getFactionTag(FactionLogic.getFactionName(player.getUniqueId()));

            TextRepresentable header = Text.of("[" + factionTag + "]" + player.getName() + ": ");
            TextRepresentable textRepresentable = Text.of(message);

            event.setMessage(header,textRepresentable);
        }

        return;
    }
}
