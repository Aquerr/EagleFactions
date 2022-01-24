package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public class DebugCommand extends AbstractCommand
{
    public DebugCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.DEBUG_MODE_PLAYERS.remove(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.DEBUG_MODE_HAS_BEEN_TURNED_OFF, GREEN)));
        }
        else
        {
            EagleFactionsPlugin.DEBUG_MODE_PLAYERS.add(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.DEBUG_MODE_HAS_BEEN_TURNED_ON, GREEN)));
        }
        return CommandResult.success();
    }
}
