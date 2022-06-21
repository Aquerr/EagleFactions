package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

public class FactionNameParser implements EFPlaceholderParser
{
    private final FactionLogic factionLogic;

    FactionNameParser(FactionLogic factionLogic)
    {
        this.factionLogic = factionLogic;
    }

    @Override
    public Optional<String> parse(ParsingContext parsingContext)
    {
        // First
        ServerPlayer serverPlayer = parsingContext.get(ServerPlayer.class)
                .orElse(null);
        if (serverPlayer != null)
            return parseByServerPlayer(serverPlayer);

        // Second
        Faction faction = parsingContext.get(Faction.class)
                .orElse(null);
        if (faction != null)
            return parseByFaction(faction);

        // else
        return Optional.empty();
    }

    private Optional<String> parseByFaction(Faction faction)
    {
        return Optional.of(faction)
                .map(Faction::getName);
    }

    private Optional<String> parseByServerPlayer(ServerPlayer serverPlayer)
    {
        return Optional.of(serverPlayer)
                .map(Identifiable::uniqueId)
                .flatMap(factionLogic::getFactionByPlayerUUID)
                .map(Faction::getName);
    }
}
