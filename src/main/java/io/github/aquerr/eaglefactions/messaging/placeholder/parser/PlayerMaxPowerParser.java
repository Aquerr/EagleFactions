package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

public class PlayerMaxPowerParser implements EFPlaceholderParser
{
    private final PlayerManager playerManager;

    PlayerMaxPowerParser(PlayerManager playerManager)
    {
        this.playerManager = playerManager;
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
                .map(Identifiable::uniqueId)
                .flatMap(playerManager::getFactionPlayer)
                .map(FactionPlayer::getMaxPower)
                .map(String::valueOf);
    }
}
