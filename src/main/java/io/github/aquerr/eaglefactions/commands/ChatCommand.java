package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.message.PluginMessages;
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
    public ChatCommand(EagleFactions plugin)
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
            if(EagleFactions.CHAT_LIST.containsKey(player.getUniqueId()))
            {
                if(optionalChatType.get().equals(ChatEnum.GLOBAL))
                {
                    EagleFactions.CHAT_LIST.remove(player.getUniqueId());
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, PluginMessages.GLOBAL_CHAT, TextColors.RESET, "!"));
                }
                else
                {
                    EagleFactions.CHAT_LIST.replace(player.getUniqueId(), EagleFactions.CHAT_LIST.get(player.getUniqueId()), optionalChatType.get());
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, optionalChatType.get(), TextColors.RESET, "!"));
                }
            }
            else
            {
                EagleFactions.CHAT_LIST.put(player.getUniqueId(), optionalChatType.get());
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, optionalChatType.get(), TextColors.RESET, "!"));
            }
        }
        else
        {
            //If player is in alliance chat or faction chat.
            if(EagleFactions.CHAT_LIST.containsKey(player.getUniqueId()))
            {
                if(EagleFactions.CHAT_LIST.get(player.getUniqueId()).equals(ChatEnum.ALLIANCE))
                {
                    EagleFactions.CHAT_LIST.replace(player.getUniqueId(), ChatEnum.ALLIANCE, ChatEnum.FACTION);
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, PluginMessages.FACTION_CHAT, TextColors.RESET, "!"));
                }
                else
                {
                    EagleFactions.CHAT_LIST.remove(player.getUniqueId());
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, PluginMessages.GLOBAL_CHAT, TextColors.RESET, "!"));
                }
            }
            else
            {
                EagleFactions.CHAT_LIST.put(player.getUniqueId(), ChatEnum.ALLIANCE);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CHANGED_CHAT_TO + " ", TextColors.GOLD, PluginMessages.ALLIANCE_CHAT, TextColors.RESET, "!"));
            }
        }
        return CommandResult.success();
    }
}