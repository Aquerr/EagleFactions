package io.github.aquerr.eaglefactions.common.commands.backup;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.concurrent.CompletableFuture;

/**
 * Command used for making backups of Eagle Factions data.
 * Factions and players are saved into HOCON files which are then compressed into a single ZIP file.
 */
public class BackupCommand extends AbstractCommand
{
    public BackupCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        //No extra checks needed. Only the player that has permission for this command should be able to use it.


        //We run it async so that It does not freeze the server.
        CompletableFuture.runAsync(() ->{
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, "Creating a backup..."));
            final boolean result = super.getPlugin().getStorageManager().createBackup();
            if (result)
            {
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Backup has been successfully created!"));
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong during creation of backup. Check your server log file for more details."));
            }
        });

        return CommandResult.success();
    }
}
