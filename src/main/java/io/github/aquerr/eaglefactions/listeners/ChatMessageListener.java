package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
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

@Singleton
public class ChatMessageListener extends GenericListener
{

    @Inject
    public ChatMessageListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, EventManager eventManager)
    {
        super(cache, settings, eagleFactions, eventManager);
    }

    @Listener
    public void onChatMessage(MessageChannelEvent.Chat event, @Root Player player)
    {
        Optional<Faction> optionalPlayerFaction = cache.getFactionByPlayer(player.getUniqueId());

        if (optionalPlayerFaction.isPresent())
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

            //Message = Prefixes + Player Name + Text
            //OriginalMessage = Player Name + Text
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

                    for (String allianceName : playerFaction.Alliances)
                    {
                        receivers.addAll(FactionLogic.getOnlinePlayers(FactionLogic.getFactionByName(allianceName)));
                    }

                    receivers.addAll(FactionLogic.getOnlinePlayers(playerFaction));

                    messageChannel = MessageChannel.fixed(receivers);
                } else if (EagleFactions.ChatList.get(player.getUniqueId()).equals(ChatEnum.Faction))
                {
                    message.append(Text.of(TextColors.GREEN, event.getRawMessage()));
                    chatTypePrefix.append(getFactionPrefix());
                    messageChannel.asMutable().clearMembers();

                    Set<MessageReceiver> receivers = new HashSet<>();

                    receivers.addAll(FactionLogic.getOnlinePlayers(playerFaction));

                    messageChannel = MessageChannel.fixed(receivers);
                }

                //Add chatType to formattedMessage
                formattedMessage.append(chatTypePrefix.build());
            } else
            {
                //If player is chatting in global chat then directly get raw message from event.
                message.append(event.getMessage().getChildren().get(1));
            }

            //Get faction prefix from Eagle Factions.
            if (settings.getPrefixOption().equals("tag"))
            {
                if (!playerFaction.Tag.toPlainSingle().equals(""))
                {
                    if (settings.areColoredTagsAllowed())
                    {
                        //Get faction's tag
                        Text factionTag = Text.builder()
                                //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                                .append(settings.getFactionPrefixStart(), playerFaction.Tag, settings.getFactionPrefixEnd())
                                .build();

                        factionPrefixText.append(factionTag);
                    } else
                    {
                        //Get faction's tag
                        Text factionTag = Text.builder()
                                //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                                .append(settings.getFactionPrefixStart(), Text.of(TextColors.GREEN, playerFaction.Tag), settings.getFactionPrefixEnd())
                                .build();

                        factionPrefixText.append(factionTag);
                    }
                }
            } else if (settings.getPrefixOption().equals("name"))
            {
                //Add faction name
                Text factionNamePrefix = Text.builder()
                        .append(settings.getFactionPrefixStart(), Text.of(TextColors.GREEN, playerFaction.Name, TextColors.RESET), settings.getFactionPrefixEnd())
                        .build();

                factionPrefixText.append(factionNamePrefix);
            }

            if (settings.shouldDisplayRank())
            {
                //Get leader prefix.
                if (playerFaction.Leader.equals(player.getUniqueId().toString()))
                {
                    Text leaderPrefix = Text.builder()
                            .append(Text.of("[", TextColors.GOLD, "Leader", TextColors.RESET, "]"))
                            .build();

                    rankPrefixText.append(leaderPrefix);
                }

                //Get officer prefix.
                if (playerFaction.Officers.contains(player.getUniqueId().toString()))
                {
                    Text officerPrefix = Text.builder()
                            .append(Text.of("[", TextColors.GOLD, "Officer", TextColors.RESET, "]"))
                            .build();

                    rankPrefixText.append(officerPrefix);
                }
            }

            if (settings.isFactionPrefixFirstInChat())
            {
                factionAndRankPrefix.append(factionPrefixText.build());
                factionAndRankPrefix.append(rankPrefixText.build());
            } else
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
                .append(settings.getFactionPrefixStart(), Text.of(TextColors.BLUE, "Alliance", TextColors.RESET), settings.getFactionPrefixEnd())
                .build();
    }

    private Text getFactionPrefix()
    {
        return Text.builder()
                .append(settings.getFactionPrefixStart(), Text.of(TextColors.GREEN, "Faction", TextColors.RESET), settings.getFactionPrefixEnd())
                .build();
    }
}
