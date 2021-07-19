package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BackupNameArgument
{
    private BackupNameArgument()
    {

    }

    public static class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<String>
    {
        private final StorageManager storageManager;

        public ValueParser(StorageManager storageManager)
        {
            this.storageManager = storageManager;
        }

        @Override
        public Optional<? extends String> parseValue(Parameter.Key<? super String> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
        {
            if(!reader.canRead())
                throw reader.createException(Component.text("Argument is not a valid backup name!"));

            final String backupName = reader.parseUnquotedString();
            if (!this.storageManager.listBackups().contains(backupName))
                throw reader.createException(Component.text("Argument is not a valid backup name!"));
            return Optional.of(backupName);
        }
    }

    public static class Completer implements ValueCompleter
    {
        private final StorageManager storageManager;

        public Completer(StorageManager storageManager)
        {
            this.storageManager = storageManager;
        }

        @Override
        public List<CommandCompletion> complete(CommandContext context, String currentInput)
        {
            final List<String> backups = this.storageManager.listBackups();
            final List<String> list = new ArrayList<>(backups);
            Collections.sort(list);

            String charSequence = currentInput.toLowerCase();
            return list.stream()
                    .filter(x->x.toLowerCase().contains(charSequence))
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        }
    }
}
