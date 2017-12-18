package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatMessageListener
{
    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player)
    {
        if(FactionLogic.getFactionName(player.getUniqueId()) != null)
        {
            MessageChannel messageChannel = event.getOriginalChannel();
            String factionName = FactionLogic.getFactionName(player.getUniqueId());

            Text.Builder formattedMessage = Text.builder();
            Text.Builder chatTypePrefix = Text.builder();
            Text.Builder factionAndRankPrefix = Text.builder();
            Text.Builder otherPluginsPrefixes = Text.builder();

            //Message = Prefixes + Player Name + Text
            //OriginalMessage = Player Name + Text
            //RawMessage = Text

            //Get the entire message and remove nickname and chat from it so prefixes from other plugins will be shown correctly.
            formattedMessage.append(event.getMessage().getChildren());
            formattedMessage.remove(event.getOriginalMessage().getChildren());

            if (EagleFactions.ChatList.containsKey(player.getUniqueId()))
            {
                if (EagleFactions.ChatList.get(player.getUniqueId()).equals(ChatEnum.Alliance))
                {
                    formattedMessage.append(Text.of(TextColors.BLUE, event.getRawMessage()));
                    chatTypePrefix.append(getAlliancePrefix());
                    messageChannel.asMutable().clearMembers();

                    Set<MessageReceiver> receivers = new HashSet<>();

                    for (String allianceName : FactionLogic.getAlliances(factionName))
                    {
                        for (UUID uuid : FactionLogic.getPlayersOnline(allianceName))
                        {
                            if(Sponge.getServer().getPlayer(uuid).isPresent())
                            {
                                receivers.add(Sponge.getServer().getPlayer(uuid).get());
                            }
                        }
                    }

                    for (UUID uuid : FactionLogic.getPlayersOnline(factionName))
                    {
                        if(Sponge.getServer().getPlayer(uuid).isPresent())
                        {
                            receivers.add(Sponge.getServer().getPlayer(uuid).get());
                        }
                    }
                    messageChannel = MessageChannel.fixed(receivers);
                }
                else if (EagleFactions.ChatList.get(player.getUniqueId()).equals(ChatEnum.Faction))
                {
                    formattedMessage.append(Text.of(TextColors.GREEN, event.getRawMessage()));
                    chatTypePrefix.append(getFactionPrefix());
                    messageChannel.asMutable().clearMembers();

                    Set<MessageReceiver> receivers = new HashSet<>();

                    for (UUID uuid : FactionLogic.getPlayersOnline(factionName))
                    {
                        if(Sponge.getServer().getPlayer(uuid).isPresent())
                        {
                            receivers.add(Sponge.getServer().getPlayer(uuid).get());
                        }
                    }

                    messageChannel = MessageChannel.fixed(receivers);
                }
            }
            else
            {
                formattedMessage.append(event.getRawMessage());
            }

            if(MainLogic.getPrefixOption().equals("tag"))
            {
                if(!FactionLogic.getFactionTag(factionName).equals("") && FactionLogic.getFactionTag(factionName) != null)
                {
                    //Get faction's tag
                    Text factionTag = Text.builder()
                            .append(Text.of("[" ,TextColors.GREEN, FactionLogic.getFactionTag(factionName), TextColors.RESET, "]"))
                            .build();

                    factionAndRankPrefix.append(factionTag);
                }
            }
            else if (MainLogic.getPrefixOption().equals("name"))
            {
                //Add faction name
                Text factionNamePrefix = Text.builder()
                        .append(Text.of("[" ,TextColors.GREEN, factionName, TextColors.RESET, "]"))
                        .build();

                factionAndRankPrefix.append(factionNamePrefix);
            }

            //Get leader prefix.
            if(FactionLogic.getLeader(factionName).equals(player.getUniqueId().toString()))
            {
                Text leaderPrefix = Text.builder()
                        .append(Text.of("[", TextColors.GOLD, "Leader", TextColors.RESET, "]"))
                        .build();

                factionAndRankPrefix.append(leaderPrefix);
            }

            //Get officer prefix.
            if(FactionLogic.getOfficers(factionName).contains(player.getUniqueId().toString()))
            {
                Text officerPrefix = Text.builder()
                        .append(Text.of("[", TextColors.GOLD, "Officer", TextColors.RESET, "]"))
                        .build();

                factionAndRankPrefix.append(officerPrefix);
            }

            //Build the whole message & print.
            Text messageToPrint = Text.builder()
                    .append(chatTypePrefix.build())
                    .append(factionAndRankPrefix.build())
                    .append(formattedMessage.build())
                    .build();


            event.setChannel(messageChannel);
            event.setMessage(messageToPrint);
        }
        return;
    }

    private Text getAlliancePrefix()
    {
        Text alliancePrefix = Text.builder()
                .append(Text.of("[", TextColors.BLUE, "Alliance", TextColors.RESET, "]"))
                .build();
        return alliancePrefix;
    }

    private Text getFactionPrefix()
    {
        Text factionPrefix = Text.builder()
                .append(Text.of("[", TextColors.GREEN, "Faction", TextColors.RESET, "]"))
                .build();
        return factionPrefix;
    }
}
