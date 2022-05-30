package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

public class FactionMaxPowerParser implements EFPlaceholderParser
{
    private final FactionLogic factionLogic;
    private final PowerManager powerManager;

    FactionMaxPowerParser(FactionLogic factionLogic, PowerManager powerManager)
    {
        this.factionLogic = factionLogic;
        this.powerManager = powerManager;
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
                .flatMap(factionLogic::getFactionByPlayerUUID)
                .map(powerManager::getFactionMaxPower)
                .map(String::valueOf);
    }
}
