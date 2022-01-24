package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class SetPowerCommand extends AbstractCommand
{
    public SetPowerCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Player selectedPlayer = context.requireOne(CommonParameters.PLAYER);
        final double power = context.requireOne(Parameter.doubleNumber().key("power").build());
        Audience audience = context.cause().audience();
        if (!(audience instanceof ServerPlayer))
            setPower(audience, selectedPlayer, (float)power);
        else
        {
            final ServerPlayer player = (ServerPlayer) audience;
            if (!super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS, RED)));
            setPower(audience, selectedPlayer, (float)power);
        }
        return CommandResult.success();
    }

    private void setPower(final Audience audience, final Player player, final float power)
    {
        super.getPlugin().getPowerManager().setPlayerPower(player.uniqueId(), power);
        audience.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.PLAYERS_POWER_HAS_BEEN_CHANGED, GREEN)));
    }
}
