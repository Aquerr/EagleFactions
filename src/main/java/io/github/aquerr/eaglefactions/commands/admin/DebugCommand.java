package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class DebugCommand extends AbstractCommand
{
    public DebugCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(player.getUniqueId()))
        {
            EagleFactionsPlugin.DEBUG_MODE_PLAYERS.remove(player.getUniqueId());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.DEBUG_MODE_HAS_BEEN_TURNED_OFF));
        }
        else
        {
            EagleFactionsPlugin.DEBUG_MODE_PLAYERS.add(player.getUniqueId());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.DEBUG_MODE_HAS_BEEN_TURNED_ON));
        }
        return CommandResult.success();
    }
}
