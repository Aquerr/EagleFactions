package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class AdminCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player) source;

            if (EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                EagleFactions.AdminList.remove(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, PluginMessages.ADMIN_MODE, TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.OFF));
                return CommandResult.success();
            } else
            {
                EagleFactions.AdminList.add(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, PluginMessages.ADMIN_MODE, TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.ON));
                return CommandResult.success();
            }
        } else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
