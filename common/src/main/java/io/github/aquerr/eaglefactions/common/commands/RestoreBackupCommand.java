package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.concurrent.CompletableFuture;

public class RestoreBackupCommand extends AbstractCommand
{
    public RestoreBackupCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final String filename = context.requireOne(Text.of("filename"));
        //No extra checks needed. Only the player that has permission for this command should be able to use it.

        //We run it async so that It does not freeze the server.
        CompletableFuture.runAsync(() ->{
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, "Restoring the backup..."));
            final boolean result = super.getPlugin().getStorageManager().restoreBackup(filename);
            if (result)
            {
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Backup has been successfully restored!"));
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Could not restore the backup. Check your server log file for more details."));
            }
        });

        return CommandResult.success();
    }
}
