package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.format.TextColors;

public class ChatMessageListener
{
    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player)
    {
        if(FactionLogic.getFactionName(player.getUniqueId()) != null)
        {
            String message = event.getMessage().toPlain();

            String fullMessage[] = message.split(":");

            String head = fullMessage[0];
            String body = fullMessage[1];

            head = head.replace(player.getName(), "");

            String factionTag = FactionLogic.getFactionTag(FactionLogic.getFactionName(player.getUniqueId()));
            if(factionTag != null)
            {
                TextRepresentable header = Text.of(head + "[", TextColors.GREEN, factionTag, TextColors.RESET, "]" + player.getName() + ":");
                TextRepresentable textRepresentable = Text.of(body);

                event.setMessage(header,textRepresentable);
            }
        }

        return;
    }
}
