package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.TextRepresentable;

import javax.xml.soap.Text;

public class ChatMessageListener
{
    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player)
    {
        if(FactionLogic.getFactionName(player.getUniqueId()) != null)
        {
            String message = event.getRawMessage().toPlain();

            String factionName = FactionLogic.getFactionName(player.getUniqueId());

            TextRepresentable header = org.spongepowered.api.text.Text.of("[" + factionName + "]" + player.getName() + ": ");
            TextRepresentable textRepresentable = org.spongepowered.api.text.Text.of(message);

            event.setMessage(header,textRepresentable);
        }

        return;
    }
}
