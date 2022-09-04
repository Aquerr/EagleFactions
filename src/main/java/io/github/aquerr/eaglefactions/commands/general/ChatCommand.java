package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class ChatCommand extends AbstractCommand
{
    private final MessageService messageService;

    public ChatCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Optional<ChatEnum> optionalChatType = context.one(Parameter.enumValue(ChatEnum.class).key("chat").build());

        ServerPlayer player = requirePlayerSource(context);
        requirePlayerFaction(player);

        if(optionalChatType.isPresent())
        {
            setChatChannel(player, optionalChatType.get());
        }
        else
        {
            //If player is in alliance chat or faction chat.
            if(EagleFactionsPlugin.CHAT_LIST.containsKey(player.uniqueId()))
            {
                if(EagleFactionsPlugin.CHAT_LIST.get(player.uniqueId()).equals(ChatEnum.ALLIANCE))
                {
                    setChatChannel(player, ChatEnum.FACTION);
                }
                else
                {
                    setChatChannel(player, ChatEnum.GLOBAL);
                }
            }
            else
            {
                setChatChannel(player, ChatEnum.ALLIANCE);
            }
        }
        return CommandResult.success();
    }

    private void setChatChannel(final ServerPlayer player, final ChatEnum chatType) {
        switch(chatType)
        {
            case GLOBAL:
                EagleFactionsPlugin.CHAT_LIST.remove(player.uniqueId());
                player.sendMessage(messageService.resolveMessageWithPrefix("command.chat.changed-chat-channel-to-global"));
                break;
            case ALLIANCE:
                EagleFactionsPlugin.CHAT_LIST.put(player.uniqueId(), chatType);
                player.sendMessage(messageService.resolveMessageWithPrefix("command.chat.changed-chat-channel-to-alliance"));
                break;
            case FACTION:
                EagleFactionsPlugin.CHAT_LIST.put(player.uniqueId(), chatType);
                player.sendMessage(messageService.resolveMessageWithPrefix("command.chat.changed-chat-channel-to-faction"));
                break;
        }
    }
}