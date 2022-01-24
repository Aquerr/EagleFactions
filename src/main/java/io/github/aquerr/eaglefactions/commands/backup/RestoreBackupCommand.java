package io.github.aquerr.eaglefactions.commands.backup;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class RestoreBackupCommand extends AbstractCommand
{
    public RestoreBackupCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final String filename = context.requireOne(Parameter.string().key("filename").build());
        //No extra checks needed. Only the player that has permission for this command should be able to use it.

        //We run it async so that It does not freeze the server.
        CompletableFuture.runAsync(() ->{
            context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text("Restoring backup ")).append(Component.text(filename, GOLD)));
            final boolean result = super.getPlugin().getStorageManager().restoreBackup(filename);
            if (result)
            {
                context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text("Backup has been successfully restored!", GREEN)));
            }
            else
            {
                context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text("Could not restore the backup. Check your server log file for more details.", RED)));
            }
        });

        return CommandResult.success();
    }
}
