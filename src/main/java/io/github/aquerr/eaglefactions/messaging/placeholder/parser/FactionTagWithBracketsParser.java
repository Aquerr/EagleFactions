package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

public class FactionTagWithBracketsParser implements EFPlaceholderParser
{
    private final FactionLogic factionLogic;
    private final ChatConfig chatConfig;

    FactionTagWithBracketsParser(FactionLogic factionLogic, ChatConfig chatConfig)
    {
        this.factionLogic = factionLogic;
        this.chatConfig = chatConfig;
    }

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
                .map(Faction::getTag)
                .map(this::buildAndSerializeTagWithBrackets);
    }

    private String buildAndSerializeTagWithBrackets(TextComponent tag)
    {
        return LegacyComponentSerializer.legacyAmpersand()
                .serialize(LinearComponents.linear(chatConfig.getFactionStartPrefix(), tag, chatConfig.getFactionEndPrefix()));
    }
}
