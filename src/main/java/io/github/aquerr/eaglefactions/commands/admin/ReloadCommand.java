package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class ReloadCommand extends AbstractCommand
{
    public ReloadCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        try
        {
            super.getPlugin().getConfiguration().reloadConfiguration();
            super.getPlugin().getStorageManager().reloadStorage();

            EagleFactionsPlugin.getPlugin().getIntegrationManager().reloadIntegrations();

            context.cause().audience().sendMessage(super.getPlugin().getMessageService().resolveMessageWithPrefix("command.reload.config-reloaded"));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return CommandResult.success();
    }
}
