package io.github.aquerr.eaglefactions.commands.management;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.world.server.ServerWorld;

public class WorldCommand implements CommandExecutor
{

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerWorld serverWorld = context.requireOne(CommonParameters.WORLD);

        context.sendMessage(Identity.nil(), Component.text(serverWorld.toString()));

        return CommandResult.success();
    }
}
