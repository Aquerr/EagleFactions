package io.github.aquerr.eaglefactions.commands.admin;

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
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SetPowerCommand extends AbstractCommand
{
    public SetPowerCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer selectedPlayer = context.requireOne(CommonParameters.PLAYER);
        final double power = context.requireOne(Parameter.doubleNumber().key("power").build());
        if (!(isPlayer(context)))
            setPower(context, selectedPlayer, (float)power);
        else
        {
            final ServerPlayer player = (ServerPlayer) context.cause().audience();
            if (!super.getPlugin().getPlayerManager().hasAdminMode(player))
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS, NamedTextColor.RED)));
            setPower(context, selectedPlayer, (float)power);
        }
        return CommandResult.success();
    }

    private void setPower(final CommandContext context, final ServerPlayer player, final float power)
    {
        super.getPlugin().getPowerManager().setPlayerPower(player.uniqueId(), power);
        context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.PLAYERS_POWER_HAS_BEEN_CHANGED, NamedTextColor.GREEN)));
    }
}
