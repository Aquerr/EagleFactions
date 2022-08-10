package io.github.aquerr.eaglefactions.commands.validator;

import io.github.aquerr.eaglefactions.PluginInfo;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.exception.CommandException;

import java.util.regex.Pattern;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class AlphaNumericFactionNameTagValidator
{
    private AlphaNumericFactionNameTagValidator()
    {
        
    }

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9]*$");

    private static final AlphaNumericFactionNameTagValidator INSTANCE = new AlphaNumericFactionNameTagValidator();

    public static AlphaNumericFactionNameTagValidator getInstance()
    {
        return INSTANCE;
    }

    public void validate(String factionName, String tag) throws CommandException
    {
        if(!ALPHANUMERIC_PATTERN.matcher(factionName).matches() || !ALPHANUMERIC_PATTERN.matcher(tag).matches())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text("Faction name and tag must be alphanumeric!", RED)));
    }

    public void validateTag(String tag) throws CommandException
    {
        if (!ALPHANUMERIC_PATTERN.matcher(tag).matches())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text("Faction name must be alphanumeric!", RED)));
    }

    public void validateFactionName(String factionName) throws CommandException
    {
        if (!ALPHANUMERIC_PATTERN.matcher(factionName).matches())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text("Faction tag must be alphanumeric!", RED)));
    }


}
