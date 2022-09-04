package io.github.aquerr.eaglefactions.commands.backup;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Command used for making backups of Eagle Factions data.
 * Factions and players are saved into HOCON files which are then compressed into a single ZIP file.
 */
public class BackupCommand extends AbstractCommand
{
    private final MessageService messageService;

    public BackupCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        //No extra checks needed. Only the player that has permission for this command should be able to use it.


        //We run it async so that It does not freeze the server.
        CompletableFuture.runAsync(() ->{
            context.sendMessage(Identity.nil(), messageService.resolveMessageWithPrefix("command.backup.create.start"));
            final Path backupPath = super.getPlugin().getStorageManager().createBackup();
            if (backupPath != null)
            {
                context.sendMessage(Identity.nil(), messageService.resolveMessageWithPrefix("command.backup.create.success", backupPath.getFileName().toString()));
            }
            else
            {
                context.sendMessage(Identity.nil(), messageService.resolveMessageWithPrefix("command.backup.create.error"));
            }
        });

        return CommandResult.success();
    }
}
