package io.github.aquerr.eaglefactions.common.commands.access;

import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class AccessPlayerCommand extends AbstractCommand
{
    public AccessPlayerCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        return CommandResult.success();
    }
}
