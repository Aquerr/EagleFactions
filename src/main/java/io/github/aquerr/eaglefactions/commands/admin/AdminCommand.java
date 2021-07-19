package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AdminCommand extends AbstractCommand
{
    public AdminCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        if (!(isPlayer(context)))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND, NamedTextColor.RED)));

        final ServerPlayer player = (ServerPlayer)context.cause().audience();
        if(super.getPlugin().getPlayerManager().hasAdminMode(player))
        {
            super.getPlugin().getPlayerManager().deactivateAdminMode(player);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.ADMIN_MODE_HAS_BEEN_TURNED_OFF, NamedTextColor.GOLD)));
        }
        else
        {
            super.getPlugin().getPlayerManager().activateAdminMode(player);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.ADMIN_MODE_HAS_BEEN_TURNED_ON, NamedTextColor.GOLD)));
        }
        return CommandResult.success();
    }
}
