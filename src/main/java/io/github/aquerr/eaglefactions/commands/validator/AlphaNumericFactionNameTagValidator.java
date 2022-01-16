package io.github.aquerr.eaglefactions.commands.validator;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.regex.Pattern;

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
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_NAME_AND_TAG_MUST_BE_ALPHANUMERIC));
    }

    public void validateTag(String tag) throws CommandException
    {
        if (!ALPHANUMERIC_PATTERN.matcher(tag).matches())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_TAG_MUST_BE_ALPHANUMERIC));
    }

    public void validateFactionName(String factionName) throws CommandException
    {
        if (!ALPHANUMERIC_PATTERN.matcher(factionName).matches())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_NAME_MUST_BE_ALPHANUMERIC));
    }


}
