package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class VersionCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        source.sendMessage (Text.of (TextColors.AQUA, PluginInfo.Name, TextColors.WHITE, " - " + PluginMessages.VERSION + " ", PluginInfo.Version));

        return CommandResult.success();
    }
}
