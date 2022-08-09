package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class DebugCommand extends AbstractCommand
{
    private final MessageService messageService;

    public DebugCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.DEBUG_MODE_PLAYERS.remove(player.uniqueId());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.debug.disabled"));
        }
        else
        {
            EagleFactionsPlugin.DEBUG_MODE_PLAYERS.add(player.uniqueId());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.debug.enabled"));
        }
        return CommandResult.success();
    }
}
