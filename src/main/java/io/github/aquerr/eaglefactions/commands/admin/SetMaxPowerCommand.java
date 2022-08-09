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

public class SetMaxPowerCommand extends AbstractCommand
{
    private final MessageService messageService;

    public SetMaxPowerCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Player selectedPlayer = context.requireOne(CommonParameters.PLAYER);
        final double power = context.requireOne(Parameter.doubleNumber().key("power").build());

        if (isServerPlayer(context.cause().audience()))
        {
            final ServerPlayer player = (ServerPlayer)context.cause().audience();
            if (!super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
                throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_ADMIN_MODE_REQUIRED);
        }
        setMaxPower(context.cause().audience(), selectedPlayer, (float) power);
        return CommandResult.success();
    }

    private void setMaxPower(Audience audience, Player player, float power)
    {
        super.getPlugin().getPowerManager().setPlayerMaxPower(player.uniqueId(), power);
        audience.sendMessage(messageService.resolveMessageWithPrefix("command.max-power.player-max-power-updated"));
    }
}
