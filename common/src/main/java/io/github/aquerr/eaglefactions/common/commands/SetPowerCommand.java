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

import java.util.Optional;

public class SetPowerCommand extends AbstractCommand
{
    public SetPowerCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Player selectedPlayer = context.requireOne("player");
        final double power = context.requireOne("power");
        if (!(source instanceof Player))
            setPower(source, selectedPlayer, (float)power);
        else
        {
            final Player player = (Player) source;
            if (!EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));
            setPower(source, selectedPlayer, (float)power);
        }
        return CommandResult.success();
    }

    private void setPower(final CommandSource source, final Player player, final float power)
    {
        super.getPlugin().getPowerManager().setPower(player.getUniqueId(), power);
        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.PLAYERS_POWER_HAS_BEEN_CHANGED));
    }
}
