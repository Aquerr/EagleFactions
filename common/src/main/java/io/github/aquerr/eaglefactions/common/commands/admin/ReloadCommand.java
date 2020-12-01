package io.github.aquerr.eaglefactions.common.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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

            if (super.getPlugin().getConfiguration().getDynmapConfig().isDynmapIntegrationEnabled() && EagleFactionsPlugin.getPlugin().getDynmapService() != null)
            {
                EagleFactionsPlugin.getPlugin().getDynmapService().reload();
            }

            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.CONFIG_HAS_BEEN_RELOADED));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return CommandResult.success();
    }
}
