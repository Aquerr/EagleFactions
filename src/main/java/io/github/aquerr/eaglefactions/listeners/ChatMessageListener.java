package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
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
            String factionName = FactionLogic.getFactionName(player.getUniqueId());

            Text.Builder factionPrefix = Text.builder();

            if(MainLogic.getPrefixOption().equals("tag"))
            {
                if(!FactionLogic.getFactionTag(factionName).equals("") && FactionLogic.getFactionTag(factionName) != null)
                {
                    //Get faction's tag
                    Text factionTag = Text.builder()
                            .append(Text.of("[" ,TextColors.GREEN, FactionLogic.getFactionTag(factionName), TextColors.RESET, "]"))
                            .build();

                    factionPrefix.append(factionTag);
                }
            }
            else if (MainLogic.getPrefixOption().equals("name"))
            {
                //Add faction name
                Text factionNamePrefix = Text.builder()
                        .append(Text.of("[" ,TextColors.GREEN, factionName, TextColors.RESET, "]"))
                        .build();

                factionPrefix.append(factionNamePrefix);
            }

            //Get leader prefix.
            if(FactionLogic.getLeader(factionName).equals(player.getUniqueId().toString()))
            {
                Text leaderPrefix = Text.builder()
                        .append(Text.of("[", TextColors.GOLD, "Leader", TextColors.RESET, "]"))
                        .build();

                factionPrefix.append(leaderPrefix);
            }

            //Get officer prefix.
            if(FactionLogic.getOfficers(factionName).contains(player.getUniqueId().toString()))
            {
                Text officerPrefix = Text.builder()
                        .append(Text.of("[", TextColors.GOLD, "Officer", TextColors.RESET, "]"))
                        .build();

                factionPrefix.append(officerPrefix);
            }

            //Build the whole message & print.
            Text messageToPrint = Text.builder()
                    .append(factionPrefix.build())
                    .append(event.getMessage())
                    .build();

            event.setMessage(messageToPrint);
        }
        return;
    }
}
