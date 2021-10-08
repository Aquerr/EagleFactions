package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BackupNameArgument extends CommandElement
{
    private EagleFactions plugin;

    public BackupNameArgument(final EagleFactions plugin, @Nullable Text key)
    {
        super(key);
        this.plugin = plugin;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException
    {
        if(!args.hasNext())
           throw args.createError(Text.of("Argument is not a valid backup name!"));

        final String backupName = args.next();
        if (!this.plugin.getStorageManager().listBackups().contains(backupName))
            throw args.createError(Text.of("Argument is not a valid backup name!"));
        return backupName;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context)
    {
        final List<String> backups = plugin.getStorageManager().listBackups();
        final List<String> list = new ArrayList<>(backups);
        Collections.sort(list);

        if (args.hasNext())
        {
            String charSequence = args.nextIfPresent().get().toLowerCase();
            return list.stream().filter(x->x.toLowerCase().contains(charSequence)).collect(Collectors.toList());
        }

        return list;
    }
}
