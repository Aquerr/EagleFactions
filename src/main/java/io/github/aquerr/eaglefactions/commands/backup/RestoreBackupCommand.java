package io.github.aquerr.eaglefactions.commands.backup;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.concurrent.CompletableFuture;

public class RestoreBackupCommand extends AbstractCommand
{
    private final MessageService messageService;

    public RestoreBackupCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final String filename = context.requireOne(Parameter.string().key("filename").build());
        //No extra checks needed. Only the player that has permission for this command should be able to use it.

        //We run it async so that It does not freeze the server.
        CompletableFuture.runAsync(() ->{
            context.sendMessage(Identity.nil(), messageService.resolveMessageWithPrefix("command.backup.restore.start", filename));
            final boolean result = super.getPlugin().getStorageManager().restoreBackup(filename);
            if (result)
            {
                context.sendMessage(Identity.nil(), messageService.resolveMessageWithPrefix("command.backup.restore.success", filename));
            }
            else
            {
                context.sendMessage(Identity.nil(), messageService.resolveMessageWithPrefix("command.backup.restore.success"));
            }
        });

        return CommandResult.success();
    }
}
