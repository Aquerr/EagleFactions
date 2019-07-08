package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ChatCommand extends AbstractCommand
{
    public ChatCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final Optional<ChatEnum> optionalChatType = context.<ChatEnum>getOne("chat");

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        Player player = (Player) source;

        if(!super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId()).isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        if(optionalChatType.isPresent())
        {
            setChatChannel(player, optionalChatType.get());
        }
        else
        {
            //If player is in alliance chat or faction chat.
            if(EagleFactionsPlugin.CHAT_LIST.containsKey(player.getUniqueId()))
            {
                if(EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()).equals(ChatEnum.ALLIANCE))
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
                EagleFactionsPlugin.CHAT_LIST.remove(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, PluginMessages.GLOBAL_CHAT, TextColors.RESET, "!"));
                break;
            case ALLIANCE:
                EagleFactionsPlugin.CHAT_LIST.put(player.getUniqueId(), chatType);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, PluginMessages.ALLIANCE_CHAT, TextColors.RESET, "!"));
                break;
            case FACTION:
                EagleFactionsPlugin.CHAT_LIST.put(player.getUniqueId(), chatType);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, PluginMessages.FACTION_CHAT, TextColors.RESET, "!"));
                break;
        }
    }
}