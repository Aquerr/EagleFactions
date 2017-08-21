package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ChatMessageListener
{
    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player)
    {
        if(FactionLogic.getFactionName(player.getUniqueId()) != null)
        {
          //  //Get full formatted and colored message.
          //  Text fullMessage = event.getMessage();
          //  EagleFactions.getEagleFactions().getLogger().info(fullMessage.toPlain());
//
          //  //Get faction's tag
          //  Text factionTag = Text.builder()
          //          .append(Text.of("[" ,TextColors.GREEN, FactionLogic.getFactionTag(FactionLogic.getFactionName(player.getUniqueId())), TextColors.RESET, "]"))
          //          .build();
          //  EagleFactions.getEagleFactions().getLogger().info(factionTag.toPlain());
//
//
//
          //  //Get message content
          //  String message = event.getMessage().toPlain();
          //  String messages[] = message.split(":");
          //  String body = messages[1];
          //  EagleFactions.getEagleFactions().getLogger().info(body);
//
          //  //Get message header (Any tags that are before player's name)
          //  Text header = fullMessage.toBuilder()
          //          .remove(Text.of(messages[0].replace(player.getName() + ":", ""))).style(fullMessage.getStyle())
          //          .remove(Text.of(body))
          //          .build();
//
          //  //Text header = Text.of("");
//
          // // for (Text child: fullMessage.toBuilder().getChildren())
          // // {
          // //     for (Text nextChild: child.getChildren())
          // //     {
          // //         EagleFactions.getEagleFactions().getLogger().info("Child w childrenie to: " + nextChild.toPlain());
          // //     }
////
          // // }
//
          //  EagleFactions.getEagleFactions().getLogger().info(fullMessage.getChildren().toString());
          //  EagleFactions.getEagleFactions().getLogger().info(header.toPlain());
//
//
          //  //Create final message with factions tag.
          //  Text messageToPrint = Text.builder()
          //          .append(header)
          //          .append(factionTag)
          //          .append(Text.of(player.getName() + ":"))
          //          .append(Text.of(body))
          //          .build();
          //  EagleFactions.getEagleFactions().getLogger().info(messageToPrint.toPlain());
//
          //  event.setMessage(messageToPrint);

              //Get faction's tag
              Text factionTag = Text.builder()
                      .append(Text.of("[" ,TextColors.GREEN, FactionLogic.getFactionTag(FactionLogic.getFactionName(player.getUniqueId())), TextColors.RESET, "]"))
                      .build();
              EagleFactions.getEagleFactions().getLogger().info(factionTag.toPlain());

            Text messageToPrint = Text.builder()
                    .append(factionTag)
                    .append(event.getMessage())
                    .build();

            event.setMessage(messageToPrint);
        }

        return;
    }
}
