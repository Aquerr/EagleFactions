package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
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
            Player player = (Player)source;

            if(EagleFactions.AdminList.contains(player.getUniqueId().toString()))
            {
                EagleFactions.AdminList.remove(player.getUniqueId().toString());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, "Admin Mode", TextColors.WHITE, " has been turned ", TextColors.GOLD, "off"));
                return CommandResult.success();
            }
            else
            {
                EagleFactions.AdminList.add(player.getUniqueId().toString());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, "Admin Mode", TextColors.WHITE, " has been turned ", TextColors.GOLD, "on"));
                return CommandResult.success();
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
