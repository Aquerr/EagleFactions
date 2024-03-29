package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AdminCommand extends AbstractCommand
{
    private final MessageService messageService;

    public AdminCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        if (player.hasPermission(PluginPermissions.CONSTANT_ADMIN_MODE))
        {
            player.sendMessage(messageService.resolveMessageWithPrefix("error.command.admin.cant-disable-because-of-constant-admin-permission"));
        }
        else
        {
            if(super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            {
                super.getPlugin().getPlayerManager().deactivateAdminMode(player.user());
                player.sendMessage(messageService.resolveMessageWithPrefix("command.admin.disabled"));
            }
            else
            {
                super.getPlugin().getPlayerManager().activateAdminMode(player.user());
                player.sendMessage(messageService.resolveMessageWithPrefix("command.admin.enabled"));
            }
        }
        return CommandResult.success();
    }
}
