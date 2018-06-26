package io.github.aquerr.eaglefactions.commands.enums;

import io.github.aquerr.eaglefactions.parsers.FactionNameArgument;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

public enum BasicCommandArgument
{
    PLAYER(GenericArguments.player(Text.of("player"))),
    FACTION(new FactionNameArgument(Text.of("faction"))),
    IDENTIFIER(GenericArguments.string(Text.of("identifier"))),
    FACTION_NAME(GenericArguments.string(Text.of("faction name"))),
    OPTIONAL_PLAYER(GenericArguments.optional(GenericArguments.player(Text.of("optional player")))),
    OPTIONAL_FACTION(GenericArguments.optional(new FactionNameArgument(Text.of("optional faction")))),
    OPTIONAL_IDENTIFIER(GenericArguments.optional(GenericArguments.string(Text.of("optional identifier")))),
    STRING_ARG1(GenericArguments.string(Text.of("arg1"))),
    STRING_ARG2(GenericArguments.string(Text.of("arg2"))),
    STRING_ARG3(GenericArguments.string(Text.of("arg3"))),
    OPTIONAL_STRING_ARG1(GenericArguments.string(Text.of("optional arg1"))),
    OPTIONAL_STRING_ARG2(GenericArguments.string(Text.of("optional arg2"))),
    OPTIONAL_STRING_ARG3(GenericArguments.string(Text.of("optional arg3")));

    private final CommandElement value;

    BasicCommandArgument(CommandElement value)
    {
        this.value = value;
    }

    public CommandElement toCommandElement(){
        return value;
    }
}
