package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class ChatCommand extends AbstractCommand
{
    public ChatCommand(EagleFactions plugin)
    {
        super(plugin);
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
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.CHANGED_CHAT_TO)).append(text(Messages.GLOBAL_CHAT, GOLD)));
                break;
            case ALLIANCE:
                EagleFactionsPlugin.CHAT_LIST.put(player.uniqueId(), chatType);
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.CHANGED_CHAT_TO)).append(text(Messages.ALLIANCE_CHAT, GOLD)));
                break;
            case FACTION:
                EagleFactionsPlugin.CHAT_LIST.put(player.uniqueId(), chatType);
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.CHANGED_CHAT_TO)).append(text(Messages.FACTION_CHAT, GOLD)));
                break;
        }
    }
}