package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

            if (super.getPlugin().getConfiguration().getDynmapConfig().isDynmapIntegrationEnabled() && EagleFactionsPlugin.getPlugin().getDynmapService() != null)
            {
                EagleFactionsPlugin.getPlugin().getDynmapService().reload();
            }

            context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.CONFIG_HAS_BEEN_RELOADED, NamedTextColor.GREEN)));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return CommandResult.success();
    }
}
