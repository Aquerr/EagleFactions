package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
            if (EagleFactions.CHAT_LIST.containsKey(player.getUniqueId()))
            {
                Set<MessageReceiver> receivers = new HashSet<>();
                Text.Builder chatTypePrefix = Text.builder();

                if (EagleFactions.CHAT_LIST.get(player.getUniqueId()).equals(ChatEnum.ALLIANCE))
                {
                    message.append(Text.of(TextColors.BLUE, event.getRawMessage()));
                    chatTypePrefix.append(getAlliancePrefix());
                    messageChannel.asMutable().clearMembers();

                    for (String allianceName : playerFaction.getAlliances())
                    {
                        Faction allyFaction = super.getPlugin().getFactionLogic().getFactionByName(allianceName);
                        if(allyFaction != null)
                            receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(allyFaction));
                    }
                    receivers.addAll(getPlugin().getFactionLogic().getOnlinePlayers(playerFaction));
                }
                else if (EagleFactions.CHAT_LIST.get(player.getUniqueId()).equals(ChatEnum.FACTION))
                {
                    message.append(Text.of(TextColors.GREEN, event.getRawMessage()));
                    chatTypePrefix.append(getFactionPrefix());
                    messageChannel.asMutable().clearMembers();
                    receivers = new HashSet<>(getPlugin().getFactionLogic().getOnlinePlayers(playerFaction));
                }

                //Add users with factions-admin mode to the collection. Admins should see all chats.
                for(final UUID adminUUID : EagleFactions.ADMIN_MODE_PLAYERS)
                {
                    final Optional<Player> optionalAdminPlayer = Sponge.getServer().getPlayer(adminUUID);
                    if(optionalAdminPlayer.isPresent())
                    {
                        receivers.add(optionalAdminPlayer.get());
                    }
                }

                messageChannel = MessageChannel.fixed(receivers);

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
                                .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                                .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                                .build();

                        factionPrefixText.append(factionTag);
                    }
                    else
                    {
                        //Get faction's tag
                        Text factionTag = Text.builder()
                                //.append(Text.of("[" ,TextColors.GREEN, playerFaction.Tag, TextColors.RESET, "]"))
                                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.GREEN, playerFaction.getTag()), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                                .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                                .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
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
                        .onHover(TextActions.showText(Text.of(TextColors.BLUE, TextStyles.ITALIC, "Click to view more information about the faction!")))
                        .onClick(TextActions.runCommand("/f info " + playerFaction.getName()))
                        .build();

                factionPrefixText.append(factionNamePrefix);
            }

            if(getPlugin().getConfiguration().getConfigFields().shouldDisplayRank())
            {
                //Get leader prefix.
                if(playerFaction.getLeader().equals(player.getUniqueId()))
                {
                    Text leaderPrefix = Text.builder()
                            .append(Text.of(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), TextColors.GOLD, PluginMessages.LEADER, TextColors.RESET, getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix()))
                            .build();

                    rankPrefixText.append(leaderPrefix);
                }
                //Get officer prefix.
                else if(playerFaction.getOfficers().contains(player.getUniqueId()))
                {
                    Text officerPrefix = Text.builder()
                            .append(Text.of(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), TextColors.GOLD, PluginMessages.OFFICER, TextColors.RESET, getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix()))
                            .build();

                    rankPrefixText.append(officerPrefix);
                }
                //Get recruit prefix.
                else if(playerFaction.getRecruits().contains(player.getUniqueId()))
                {
                    Text recruitPrefix = Text.builder()
                            .append(Text.of(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), TextColors.GOLD, PluginMessages.RECRUIT, TextColors.RESET, getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix()))
                            .build();

                    rankPrefixText.append(recruitPrefix);
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
                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.BLUE, PluginMessages.ALLIANCE_CHAT, TextColors.RESET), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                .build();
    }

    private Text getFactionPrefix()
    {
        return Text.builder()
                .append(getPlugin().getConfiguration().getConfigFields().getFactionStartPrefix(), Text.of(TextColors.GREEN, PluginMessages.FACTION_CHAT, TextColors.RESET), getPlugin().getConfiguration().getConfigFields().getFactionEndPrefix())
                .build();
    }
}
