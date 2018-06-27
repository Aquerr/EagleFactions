package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.config.Configuration;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

public class ReloadCommand implements CommandExecutor
{
    @Inject
    private Configuration configuration;

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        try
        {
            configuration.load();
            //FactionLogic.reload();

            source.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.CONFIGS_HAS_BEEN_RELOADED));
        } catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return CommandResult.success();
    }
}
