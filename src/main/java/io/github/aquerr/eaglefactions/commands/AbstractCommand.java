package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public abstract class AbstractCommand implements CommandExecutor
{
    private EagleFactions _eagleFactions;

    public AbstractCommand(EagleFactions plugin)
    {
        this._eagleFactions = plugin;
    }

    public EagleFactions getPlugin()
    {
        return _eagleFactions;
    }

    @Override
    public abstract CommandResult execute(CommandSource source, CommandContext context) throws CommandException;
}
