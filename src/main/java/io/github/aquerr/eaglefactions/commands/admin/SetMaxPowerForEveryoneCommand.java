package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SetMaxPowerForEveryoneCommand extends AbstractCommand
{
    private final MessageService messageService;

    public SetMaxPowerForEveryoneCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final double power = context.requireOne(Parameter.doubleNumber().key("power").build());

        if (isServerPlayer(context.cause().audience()))
        {
            final ServerPlayer player = (ServerPlayer) context.cause().audience();
            if (!super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
                throw this.messageService.resolveExceptionWithMessage(EFMessageService.ERROR_ADMIN_MODE_REQUIRED);
        }
        setMaxPower(context.cause().audience(), (float) power);
        return CommandResult.success();
    }

    private void setMaxPower(Audience audience, float power)
    {
        for (final FactionPlayer factionPlayer : super.getPlugin().getPlayerManager().getServerPlayers())
        {
            super.getPlugin().getPowerManager().setPlayerMaxPower(factionPlayer.getUniqueId(), power);
        }
        audience.sendMessage(messageService.resolveMessageWithPrefix("command.max-power.max-power-for-everyone-updated"));
    }
}
