package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class AdminCommand extends AbstractCommand
{
    public AdminCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
        {
            EagleFactionsPlugin.ADMIN_MODE_PLAYERS.remove(player.getUniqueId());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, Messages.ADMIN_MODE_HAS_BEEN_TURNED_OFF));
        }
        else
        {
            EagleFactionsPlugin.ADMIN_MODE_PLAYERS.add(player.getUniqueId());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, Messages.ADMIN_MODE_HAS_BEEN_TURNED_ON));
        }
        return CommandResult.success();
    }
}
