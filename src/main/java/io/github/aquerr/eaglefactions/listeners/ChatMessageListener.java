package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ChatMessageListener extends AbstractListener
{
    public ChatMessageListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player)
    {
        Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(optionalPlayerFaction.isPresent())
        {
            MessageChannel messageChannel = event.getOriginalChannel();
            Faction playerFaction = optionalPlayerFaction.get();

            Text.Builder formattedMessage = Text.builder();

            Text.Builder factionAndRankPrefix = Text.builder();
            Text.Builder otherPrefixesAndPlayer = Text.builder();
            Text.Builder factionPrefixText = Text.builder();
            Text.Builder rankPrefixText = Text.builder();
            //Text.Builder playerText = Text.builder();
            Text.Builder message = Text.builder();

            //Message = Prefixes + Player NAME + Text
            //OriginalMessage = Player NAME + Text
            //RawMessage = Text

            //Get player name
            //playerText.append(event.getOriginalMessage().getChildren().get(0));

            //Get Other Plugin Prefixes and Nickname from message.
            otherPrefixesAndPlayer.append(event.getMessage().getChildren().get(0));

            //Get ChatType from Eagle Factions
            //and add it to the formattedMessage
            if (EagleFactions.ChatList.containsKey(player.getUniqueId()))
            {
                Text.Builder chatTypePrefix = Text.builder();

                if (EagleFactions.ChatList.get(player.getUniqueId()).equals(ChatEnum.Alliance))
                {
                    message.append(Text.of(TextColors.BLUE, event.getRawMessage()));
                    chatTypePrefix.append(getAlliancePrefix());
                    messageChannel.asMutable().clearMembers();

                    Set<MessageReceiver> receivers = new HashSet<>();

                    //TODO: Add option to style prefixes by user form config file.

                    for (String allianceName : playerFaction.getAlliances())
                    {
                        receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(getPlugin().getFactionLogic().getFactionByName(allianceName)));
                    }

                    receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(playerFaction));

                    messageChannel = MessageChannel.fixed(receivers);
                }
                else if (EagleFactions.ChatList.get(player.getUniqueId()).equals(ChatEnum.Faction))
                {
                    message.append(Text.of(TextColors.GREEN, event.getRawMessage()));
                    chatTypePrefix.append(getFactionPrefix());
                    messageChannel.asMutable().clearMembers();

                    Set<MessageReceiver> receivers = new HashSet<>();

                    receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(playerFaction));

                    messageChannel = MessageChannel.fixed(receivers);
                }

                //Add chatType to formattedMessage
                formattedMessage.append(chatTypePrefix.build());
            }
            else
            {
                //If player is chatting in global chat then directly get raw message from event.
                message.append(event.getMessage().getChildren().get(1));
            }

            //Get faction prefix from Eagle Factions.
            if(getPlugin().getConfiguration().getConfigFields().getChatPrefixType().equals("tag"))
            {
                if(!playerFaction.getTag().toPlainSingle().equals(""))
                {
                    if (getPlugin().getConfiguration().getConfigFields().canColorTags())
                    {
                        //Get faction's tag
                        Text factionTag = Text.builder()
                                //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), playerFaction.getTag(), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                                .build();

                        factionPrefixText.append(factionTag);
                    }
                    else
                    {
                        //Get faction's tag
                        Text factionTag = Text.builder()
                                //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.GREEN, playerFaction.getTag()), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                                .build();

                        factionPrefixText.append(factionTag);
                    }
                }
            }
            else if (getPlugin().getConfiguration().getConfigFields().getChatPrefixType().equals("name"))
            {
                //Add faction name
                Text factionNamePrefix = Text.builder()
                        .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.GREEN, playerFaction.getName(), TextColors.RESET), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                        .build();

                factionPrefixText.append(factionNamePrefix);
            }

            if(getPlugin().getConfiguration().getConfigFields().shouldDisplayRank())
            {
                //Get leader prefix.
                if(playerFaction.getLeader().equals(player.getUniqueId()))
                {
                    Text leaderPrefix = Text.builder()
                            .append(Text.of("[", TextColors.GOLD, "Leader", TextColors.RESET, "]"))
                            .build();

                    rankPrefixText.append(leaderPrefix);
                }

                //Get officer prefix.
                if(playerFaction.getOfficers().contains(player.getUniqueId()))
                {
                    Text officerPrefix = Text.builder()
                            .append(Text.of("[", TextColors.GOLD, "Officer", TextColors.RESET, "]"))
                            .build();

                    rankPrefixText.append(officerPrefix);
                }
            }

            if (getPlugin().getConfiguration().getConfigFields().isFactionPrefixFirstInChat())
            {
                factionAndRankPrefix.append(factionPrefixText.build());
                factionAndRankPrefix.append(rankPrefixText.build());
            }
            else
            {
                factionAndRankPrefix.append(rankPrefixText.build());
                factionAndRankPrefix.append(factionPrefixText.build());
            }

            //Add faction tag and faction rank
            formattedMessage.append(factionAndRankPrefix.build());
            //Add Other Plugins Prefixes
            formattedMessage.append(otherPrefixesAndPlayer.build());
            //Add player name
            //formattedMessage.append(playerText.build());
            //Add message
            formattedMessage.append(message.build());

            //Build message & print it.
            Text messageToPrint = Text.builder()
                    .append(formattedMessage.build())
                    .build();

            event.setChannel(messageChannel);
            event.setMessage(messageToPrint);
        }
        return;
    }

    private Text getAlliancePrefix()
    {
        return Text.builder()
                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.BLUE, "Alliance", TextColors.RESET), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                .build();
    }

    private Text getFactionPrefix()
    {
        return Text.builder()
                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.GREEN, "Faction", TextColors.RESET), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                .build();
    }
}
