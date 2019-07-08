package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public abstract class AbstractCommand implements CommandExecutor
{
    private EagleFactionsPlugin _eagleFactions;

    public AbstractCommand(EagleFactionsPlugin plugin)
    {
        this._eagleFactions = plugin;
    }

    public EagleFactionsPlugin getPlugin()
    {
        return _eagleFactions;
    }

    @Override
    public abstract CommandResult execute(CommandSource source, CommandContext context) throws CommandException;
}
