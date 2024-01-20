package io.github.aquerr.eaglefactions.messaging;

import io.github.aquerr.eaglefactions.api.exception.RankNotExistsException;

import java.util.Map;
import java.util.Optional;

public class ExceptionMesssageMapper
{
    private static final Map<Class<?>, String> EXCEPTION_MESSAGE_KEY_MAP = Map.of(
            RankNotExistsException.class, "error.ran-not-exists"
    );

    public static Optional<String> mapToMessageKey(Throwable throwable)
    {
        return Optional.ofNullable(EXCEPTION_MESSAGE_KEY_MAP.get(throwable.getClass()));
    }
}
