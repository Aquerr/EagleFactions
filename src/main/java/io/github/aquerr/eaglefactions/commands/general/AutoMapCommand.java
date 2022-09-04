package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AutoMapCommand extends AbstractCommand
{
    private final MessageService messageService;

    public AutoMapCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        if(EagleFactionsPlugin.AUTO_MAP_LIST.contains(player.uniqueId()))
        {
            EagleFactionsPlugin.AUTO_MAP_LIST.remove(player.uniqueId());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.auto-map.disabled"));
        }
        else
        {
            EagleFactionsPlugin.AUTO_MAP_LIST.add(player.uniqueId());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.auto-map.enabled"));
        }

        return CommandResult.success();
    }
}
