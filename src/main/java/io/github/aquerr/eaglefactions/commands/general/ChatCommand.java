package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

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
        if(!super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId()).isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

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

    private void setChatChannel(final Player player, final ChatEnum chatType) {
        switch(chatType)
        {
            case GLOBAL:
                EagleFactionsPlugin.CHAT_LIST.remove(player.uniqueId());
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.CHANGED_CHAT_TO)).append(Component.text(Messages.GLOBAL_CHAT, NamedTextColor.GOLD)));
                break;
            case ALLIANCE:
                EagleFactionsPlugin.CHAT_LIST.put(player.uniqueId(), chatType);
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.CHANGED_CHAT_TO)).append(Component.text(Messages.ALLIANCE_CHAT, NamedTextColor.GOLD)));
                break;
            case FACTION:
                EagleFactionsPlugin.CHAT_LIST.put(player.uniqueId(), chatType);
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.CHANGED_CHAT_TO)).append(Component.text(Messages.FACTION_CHAT, NamedTextColor.GOLD)));
                break;
        }
    }
}