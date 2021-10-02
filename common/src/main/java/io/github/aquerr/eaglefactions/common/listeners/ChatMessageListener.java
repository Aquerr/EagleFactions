package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.messaging.chat.AllianceMessageChannelImpl;
import io.github.aquerr.eaglefactions.common.messaging.chat.ChatMessageHelper;
import io.github.aquerr.eaglefactions.common.messaging.chat.FactionMessageChannelImpl;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;
import org.spongepowered.api.util.Tristate;

import java.util.*;

public class ChatMessageListener extends AbstractListener
{
    private final ChatConfig chatConfig;

    public ChatMessageListener(EagleFactions plugin)
    {
        super(plugin);
        this.chatConfig = plugin.getConfiguration().getChatConfig();
    }

    @Listener
    @IsCancelled(Tristate.FALSE)
    public void onChatMessage(final MessageChannelEvent.Chat event, final @Root Player player)
    {
        MessageChannel messageChannel = event.getChannel().orElse(event.getOriginalChannel());
        final MessageEvent.MessageFormatter messageFormatter = event.getFormatter();

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(!optionalPlayerFaction.isPresent())
        {
            //Suppress message for other people
            if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
            {
                final MessageChannel suppressedChannel = ChatMessageHelper.removeFactionChatPlayersFromChannel(messageChannel);
                event.setChannel(suppressedChannel);
            }

            //Add non-faction prefix tag.
            if(!this.chatConfig.getNonFactionPlayerPrefix().toPlain().equals(""))
            {
                messageFormatter.getHeader().insert(0, new SimpleTextTemplateApplier(TextTemplate.of(this.chatConfig.getNonFactionPlayerPrefix())));
            }
            return;
        }

        //Code below is for players that have a faction.
        final Faction playerFaction = optionalPlayerFaction.get();
        final Text.Builder factionAndRankPrefix = Text.builder();
        final Text.Builder factionPrefix = Text.builder();
        final Text.Builder rankPrefix = Text.builder();
        final Text.Builder chatTypePrefix = Text.builder();
        final Text.Builder message = Text.builder();

        ChatEnum chatType = Optional.ofNullable(EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()))
                .orElse(ChatEnum.GLOBAL);

        //Message = Prefixes + Player NAME + Text
        //OriginalMessage = Player NAME + Text
        //RawMessage = Text

        chatTypePrefix.append(ChatMessageHelper.getChatPrefix(player));

        switch(chatType)
        {
            case FACTION:
            {
                message.append(Text.of(TextColors.GREEN, messageFormatter.getBody().format()));
                messageChannel = FactionMessageChannelImpl.forFaction(playerFaction);
                break;
            }
            case ALLIANCE:
            {
                message.append(Text.of(TextColors.BLUE, messageFormatter.getBody().format()));
                messageChannel = AllianceMessageChannelImpl.forFaction(playerFaction);
                break;
            }
            case GLOBAL:
            default:
                //If player is chatting in global chat then directly get raw message from event.
                message.append(messageFormatter.getBody().format());

                //Suppress message for other factions if someone is in the faction's chat.
                if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
                {
                    messageChannel = ChatMessageHelper.removeFactionChatPlayersFromChannel(messageChannel);
                }
        }

        final Text fPrefix = ChatMessageHelper.getFactionPrefix(playerFaction);
        if (fPrefix != null)
            factionPrefix.append(fPrefix);

        final Text rPrefix = ChatMessageHelper.getRankPrefix(chatType, playerFaction, player);
        if (rPrefix != null)
            rankPrefix.append(rPrefix);

        if (this.chatConfig.isFactionPrefixFirstInChat())
        {
            factionAndRankPrefix.append(factionPrefix.build());
            factionAndRankPrefix.append(rankPrefix.build());
        }
        else
        {
            factionAndRankPrefix.append(rankPrefix.build());
            factionAndRankPrefix.append(factionPrefix.build());
        }

        messageFormatter.getHeader().insert(0, new SimpleTextTemplateApplier(TextTemplate.of(factionAndRankPrefix)));
        messageFormatter.getHeader().insert(0, new SimpleTextTemplateApplier(TextTemplate.of(chatTypePrefix)));
        messageFormatter.setBody(message.build());

        event.setChannel(messageChannel);
    }
}
