package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import io.github.aquerr.eaglefactions.api.logic.FactionLogic;

import java.util.Optional;

public class FactionTagWithBracketsParser implements EFPlaceholderParser
{
    private final FactionLogic factionLogic;

    FactionTagWithBracketsParser(FactionLogic factionLogic)
    {
        this.factionLogic = factionLogic;
    }

    @Override
    public Optional<String> parse(ParsingContext parsingContext)
    {
        return Optional.empty();
    }
}
