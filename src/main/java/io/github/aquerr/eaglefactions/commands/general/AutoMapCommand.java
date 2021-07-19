package io.github.aquerr.eaglefactions.commands.general;

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

public class AutoMapCommand extends AbstractCommand
{
    public AutoMapCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        if(EagleFactionsPlugin.AUTO_MAP_LIST.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.AUTO_MAP_LIST.remove(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.AUTO_MAP_HAS_BEEN_TURNED_OFF, NamedTextColor.GREEN)));
        }
        else
        {
            EagleFactionsPlugin.AUTO_MAP_LIST.add(player.uniqueId());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.AUTO_MAP_HAS_BEEN_TURNED_ON, NamedTextColor.GREEN)));
        }

        return CommandResult.success();
    }
}
