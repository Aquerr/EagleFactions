package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public class ReloadCommand extends AbstractCommand
{
    public ReloadCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        try
        {
            super.getPlugin().getConfiguration().reloadConfiguration();
            super.getPlugin().getStorageManager().reloadStorage();

            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CONFIGS_HAS_BEEN_RELOADED));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return CommandResult.success();
    }
}
