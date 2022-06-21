package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class ParsingContext
{
    //TODO: Not used yet...
    final Optional<String> argument;
    private final Map<Class<?>, Object> params;

    ParsingContext(Optional<String> argument, Map<Class<?>, Object> params)
    {
        this.argument = argument;
        this.params = Collections.unmodifiableMap(params);
    }

    public <T> Optional<T> get(Class<T> clazz)
    {
        Object object = this.params.get(clazz);
        return Optional.ofNullable((T)object);
    }

    public Optional<String> getArgument()
    {
        return argument;
    }
}
