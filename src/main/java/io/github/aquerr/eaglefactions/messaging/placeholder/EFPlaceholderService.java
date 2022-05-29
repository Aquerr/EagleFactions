package io.github.aquerr.eaglefactions.messaging.placeholder;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlaceholderService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.registry.RegistryTypes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Eagle Factions' placeholder service that interacts with Sponge's placeholder system.
 */
public class EFPlaceholderService
{
    private final EagleFactions plugin;

//    private final Map<Placeholder, EFPlaceholderService> placeholderParsers = new HashMap<>();

    public EFPlaceholderService(EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    private List<EFPlaceholderParser> getDefaultPlaceholders()
    {
        List<EFPlaceholderParser> efPlaceholderParserList = new ArrayList<>();
        efPlaceholderParserList.add(new EFPlaceholderParser("faction_name", this::getFactionName));
        efPlaceholderParserList.add(new EFPlaceholderParser("faction_tag", this::getFactionTag));
        efPlaceholderParserList.add(new EFPlaceholderParser("faction_tag_with_brackets", this::getFactionTagWithBrackets));
        efPlaceholderParserList.add(new EFPlaceholderParser("faction_power", this::getFactionPower));
        efPlaceholderParserList.add(new EFPlaceholderParser("faction_maxpower", this::getFactionMaxPower));
        efPlaceholderParserList.add(new EFPlaceholderParser("faction_last_online", this::getFactionLastOnline));
        efPlaceholderParserList.add(new EFPlaceholderParser("claims_count", this::getFactionClaimCount));
        efPlaceholderParserList.add(new EFPlaceholderParser("officers_count", this::getFactionOfficerCount));
        efPlaceholderParserList.add(new EFPlaceholderParser("members_count", this::getFactionMemberCount));
        efPlaceholderParserList.add(new EFPlaceholderParser("recruits_count", this::getFactionRecruitCount));
        efPlaceholderParserList.add(new EFPlaceholderParser("alliances", this::getFactionAlliances));
        efPlaceholderParserList.add(new EFPlaceholderParser("enemies", this::getFactionEnemies));
        efPlaceholderParserList.add(new EFPlaceholderParser("truce", this::getFactionTruce));

        efPlaceholderParserList.add(new EFPlaceholderParser("player_power", this::getPlayerPower));
        efPlaceholderParserList.add(new EFPlaceholderParser("player_maxpower", this::getPlayerMaxPower));
        efPlaceholderParserList.add(new EFPlaceholderParser("player_last_online", this::getPlayerLastOnline));

        return efPlaceholderParserList;
    }

    public void onRegisterPlaceholderEvent(RegisterRegistryValueEvent.GameScoped event)
    {
        RegisterRegistryValueEvent.RegistryStep<PlaceholderParser> placeholderParserRegistryStep = event.registry(RegistryTypes.PLACEHOLDER_PARSER);
        List<EFPlaceholderParser> defaultPlaceholderParsers = getDefaultPlaceholders();

        //Register placeholders
        for (EFPlaceholderParser efPlaceholderParser : defaultPlaceholderParsers)
        {
            placeholderParserRegistryStep.register(ResourceKey.of("eaglefactions", efPlaceholderParser.getPlaceholderName()), (context) -> efPlaceholderParser.getParsingFunciton().apply(context));
        }
    }

//    private Component processPlaceholderContext(final PlaceholderContext placeholderContext, String placeholderName)
//    {
//        ServerPlayer serverPlayer = placeholderContext.associatedObject()
//            .filter(ServerPlayer.class::isInstance)
//            .map(ServerPlayer.class::cast)
//            .orElse(null);
//
//        Faction faction = placeholderContext.associatedObject()
//                .filter(Faction.class::isInstance)
//                .map(Faction.class::cast)
//                .orElse(null);
//
//        Map<ParsingObjectType, Object> parsingContext = new HashMap<>();
//        parsingContext.put(ParsingObjectType.FACTION_CLASS, faction);
//        parsingContext.put(ParsingObjectType.SERVER_PLAYER_CLASS, serverPlayer);
//
//        parsingContext.get(ParsingObjectType.FACTION_CLASS)
//
//        return placeholdersMap.get(placeholderName).parse(parsingContext);
//    }

    private Component getFactionTagWithBrackets(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(serverPlayer -> this.plugin.getConfiguration().getChatConfig().getFactionStartPrefix()
                        .append(getFactionTag(context))
                        .append(this.plugin.getConfiguration().getChatConfig().getFactionEndPrefix()))
                .orElse(Component.empty());
    }

    private Component getFactionName(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getName)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionTag(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getTag)
                .orElse(Component.empty());
    }

    private Component getFactionPower(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(this.plugin.getPowerManager()::getFactionPower)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionMaxPower(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(this.plugin.getPowerManager()::getFactionMaxPower)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionLastOnline(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getLastOnline)
                .map(instant -> Component.text(instant.toString()))
                .orElse(Component.empty());
    }

    private Component getFactionClaimCount(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getClaims)
                .map(Set::size)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionOfficerCount(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getOfficers)
                .map(Set::size)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionMemberCount(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getMembers)
                .map(Set::size)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionRecruitCount(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getRecruits)
                .map(Set::size)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionAlliances(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getAlliances)
                .map(alliances -> String.join(", ", alliances))
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionEnemies(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getEnemies)
                .map(alliances -> String.join(", ", alliances))
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getFactionTruce(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getFactionLogic()::getFactionByPlayerUUID)
                .map(Faction::getTruces)
                .map(alliances -> String.join(", ", alliances))
                .map(Component::text)
                .orElse(Component.empty());
    }

    //
    //FactionPlayer placeholder methods starts here.
    //

    private Component getPlayerPower(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getPlayerManager()::getFactionPlayer)
                .map(FactionPlayer::getPower)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getPlayerMaxPower(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::uniqueId)
                .flatMap(this.plugin.getPlayerManager()::getFactionPlayer)
                .map(FactionPlayer::getMaxPower)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private Component getPlayerLastOnline(final PlaceholderContext context)
    {
        return context.associatedObject()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .flatMap(player -> player.get(Keys.LAST_DATE_JOINED))
                .map(instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                .map(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")::format)
                .map(Component::text)
                .orElse(Component.empty());
    }

    private static final class EFPlaceholderParser
    {
        private final String placeholderName;
        private final Function<PlaceholderContext, Component> parsingFunciton;

        public EFPlaceholderParser(String placeholderName, Function<PlaceholderContext, Component> parsingFunction)
        {
            this.placeholderName = placeholderName;
            this.parsingFunciton = parsingFunction;
        }

        public String getPlaceholderName()
        {
            return placeholderName;
        }

        public Function<PlaceholderContext, Component> getParsingFunciton()
        {
            return parsingFunciton;
        }
    }
}
