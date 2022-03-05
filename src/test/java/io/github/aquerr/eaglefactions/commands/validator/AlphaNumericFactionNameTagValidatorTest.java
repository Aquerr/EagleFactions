package io.github.aquerr.eaglefactions.commands.validator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.spongepowered.api.command.CommandException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlphaNumericFactionNameTagValidatorTest
{
    private final AlphaNumericFactionNameTagValidator validator = AlphaNumericFactionNameTagValidator.getInstance();

    @MethodSource(value = "exampleWrongStrings")
    @ParameterizedTest
    void validateShouldThrowExceptionWhenFactionNameAndTagIsWrong(String text)
    {
        assertThrows(CommandException.class, () -> validator.validate(text, text));
    }

    @MethodSource(value = "exampleGoodStrings")
    @ParameterizedTest
    void validateShouldNotThrowExceptionWhenFactionNameAndTagIsGood(String text)
    {
        assertDoesNotThrow(() -> validator.validate(text, text));
    }

    @MethodSource(value = "exampleWrongStrings")
    @ParameterizedTest
    void validateFactionNameShouldThrowExceptionWhenFactionNameIsWrong(String factionName)
    {
        assertThrows(CommandException.class, () -> validator.validateFactionName(factionName));
    }

    @MethodSource(value = "exampleWrongStrings")
    @ParameterizedTest
    void validateTagShouldThrowExceptionWhenTagIsWrong(String tag)
    {
        assertThrows(CommandException.class, () -> validator.validateTag(tag));
    }

    @MethodSource(value = "exampleGoodStrings")
    @ParameterizedTest
    void validateFactionNameShouldNotThrowExceptionWhenFactionNameIsGood(String factionName)
    {
        assertDoesNotThrow(() -> validator.validateFactionName(factionName));
    }

    @MethodSource(value = "exampleGoodStrings")
    @ParameterizedTest
    void validateTagShouldNotThrowExceptionWhenTagIsGood(String tag)
    {
        assertDoesNotThrow(() -> validator.validateTag(tag));
    }

    private static List<String> exampleWrongStrings()
    {
        return Arrays.asList(null, "", "#!@#@!#");
    }

    private static List<String> exampleGoodStrings()
    {
        return Arrays.asList("123123", "aBc123", "123AbC");
    }
}