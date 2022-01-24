package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

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

            if (super.getPlugin().getConfiguration().getDynmapConfig().isDynmapIntegrationEnabled() && EagleFactionsPlugin.getPlugin().getDynmapService() != null)
            {
                EagleFactionsPlugin.getPlugin().getDynmapService().reload();
            }

            context.cause().audience().sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.CONFIG_HAS_BEEN_RELOADED, GREEN)));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return CommandResult.success();
    }
}
