package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

public class FactionAlliancesParser implements EFPlaceholderParser
{
    private final FactionLogic factionLogic;

    FactionAlliancesParser(FactionLogic factionLogic)
    {
        this.factionLogic = factionLogic;
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
                .map(Faction::getAlliances)
                .map(alliances -> String.join(", ", alliances));
    }
}
