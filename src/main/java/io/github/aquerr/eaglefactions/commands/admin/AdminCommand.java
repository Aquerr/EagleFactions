package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class AdminCommand extends AbstractCommand
{
    public AdminCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        if(super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
        {
            super.getPlugin().getPlayerManager().deactivateAdminMode(player.user());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.ADMIN_MODE_HAS_BEEN_TURNED_OFF, GOLD)));
        }
        else
        {
            super.getPlugin().getPlayerManager().activateAdminMode(player.user());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.ADMIN_MODE_HAS_BEEN_TURNED_ON, GOLD)));
        }
        return CommandResult.success();
    }
}
