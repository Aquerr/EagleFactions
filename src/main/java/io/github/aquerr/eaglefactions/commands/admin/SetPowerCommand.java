package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SetPowerCommand extends AbstractCommand
{
    private final MessageService messageService;

    public SetPowerCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
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
                throw this.messageService.resolveExceptionWithMessage(EFMessageService.ERROR_ADMIN_MODE_REQUIRED);
            setPower(audience, selectedPlayer, (float)power);
        }
        return CommandResult.success();
    }

    private void setPower(final Audience audience, final Player player, final float power)
    {
        super.getPlugin().getPowerManager().setPlayerPower(player.uniqueId(), power);
        audience.sendMessage(this.messageService.resolveMessageWithPrefix("command.power.player-power-updated"));
    }
}
