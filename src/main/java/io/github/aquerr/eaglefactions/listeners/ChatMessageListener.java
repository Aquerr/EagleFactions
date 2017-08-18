package io.github.aquerr.eaglefactions.listeners;

import com.sun.xml.internal.ws.client.sei.ResponseBuilder;
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
            //Get full formatted and colored message.
            Text fullMessage = event.getMessage();

            //Get faction's tag
            Text factionTag = Text.builder()
                    .append(Text.of("[" ,TextColors.GREEN, FactionLogic.getFactionTag(FactionLogic.getFactionName(player.getUniqueId())), TextColors.RESET, "]"))
                    .build();

            //Get message content
            String message = event.getMessage().toPlain();
            String messages[] = message.split(":");
            String body = messages[1];

            //Get message header (Any tags that are before player's name)
            Text header = fullMessage.toBuilder()
                    .remove(Text.of(player.getName() + ":"))
                    .remove(Text.of(body))
                    .build();

            //Create final message with factions tag.
            Text messageToPrint = Text.builder()
                    .append(header)
                    .append(factionTag)
                    .append(Text.of(player.getName() + ":"))
                    .append(Text.of(body))
                    .build();

            event.setMessage(messageToPrint);
        }

        return;
    }
}
