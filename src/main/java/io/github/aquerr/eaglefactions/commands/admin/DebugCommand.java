package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
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

public class DebugCommand extends AbstractCommand
{
    public DebugCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        if (!(isPlayer(context)))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND, NamedTextColor.RED)));

        final ServerPlayer player = (ServerPlayer)context.cause().audience();
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.DEBUG_MODE_PLAYERS.remove(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.DEBUG_MODE_HAS_BEEN_TURNED_OFF)));
        }
        else
        {
            EagleFactionsPlugin.DEBUG_MODE_PLAYERS.add(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.DEBUG_MODE_HAS_BEEN_TURNED_ON)));
        }
        return CommandResult.success();
    }
}
