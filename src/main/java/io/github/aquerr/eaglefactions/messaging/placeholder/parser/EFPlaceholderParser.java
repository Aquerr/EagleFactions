package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import java.util.Optional;

public interface EFPlaceholderParser
{
    Optional<String> parse(ParsingContext parsingContext);
}
