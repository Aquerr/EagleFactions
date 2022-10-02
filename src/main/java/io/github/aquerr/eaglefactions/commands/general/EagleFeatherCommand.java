package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.entities.EagleFeatherImpl;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class EagleFeatherCommand extends AbstractCommand
{
    public EagleFeatherCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer player = requirePlayerSource(context);
        player.inventory().offer(new EagleFeatherImpl().asItemStack());
        return CommandResult.success();
    }
}
