package io.github.aquerr.eaglefactions.commands.validator;

import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.exception.CommandException;

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
            throw EFMessageService.getInstance().resolveExceptionWithMessage("error.validation.faction-name-and-tag-must-be-alphanumeric");
    }

    public void validateTag(String tag) throws CommandException
    {
        if (!ALPHANUMERIC_PATTERN.matcher(tag).matches())
            throw EFMessageService.getInstance().resolveExceptionWithMessage("error.validation.faction-name-must-be-alphanumeric");
    }

    public void validateFactionName(String factionName) throws CommandException
    {
        if (!ALPHANUMERIC_PATTERN.matcher(factionName).matches())
            throw EFMessageService.getInstance().resolveExceptionWithMessage("error.validation.faction-tag-must-be-alphanumeric");
    }


}
