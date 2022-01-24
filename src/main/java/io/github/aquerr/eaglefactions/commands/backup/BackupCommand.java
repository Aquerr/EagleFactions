package io.github.aquerr.eaglefactions.commands.backup;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

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
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        //No extra checks needed. Only the player that has permission for this command should be able to use it.


        //We run it async so that It does not freeze the server.
        CompletableFuture.runAsync(() ->{
            context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(text("Creating a backup...")));
            final boolean result = super.getPlugin().getStorageManager().createBackup();
            if (result)
            {
                context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(text("Backup has been successfully created!", GREEN)));
            }
            else
            {
                context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(text("Something went wrong during creation of backup. Check your server log file for more details.", RED)));
            }
        });

        return CommandResult.success();
    }
}
