package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class PlayerLastOnlineParser implements EFPlaceholderParser
{

    PlayerLastOnlineParser()
    {

    }

    @Override
    public Optional<String> parse(ParsingContext parsingContext)
    {
        ServerPlayer serverPlayer = parsingContext.get(ServerPlayer.class)
                .orElse(null);
        if (serverPlayer != null)
            return parseByServerPlayer(serverPlayer);
        return Optional.empty();
    }

    private Optional<String> parseByServerPlayer(ServerPlayer serverPlayer)
    {
        return Optional.of(serverPlayer)
                .flatMap(player -> player.get(Keys.LAST_DATE_JOINED))
                .map(instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                .map(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")::format);
    }
}
