package io.github.aquerr.eaglefactions.messaging.placeholder.parser;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.placeholder.PlaceholderService;
import io.github.aquerr.eaglefactions.api.messaging.placeholder.Placeholder;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Eagle Factions' placeholder service that interacts with Sponge's placeholder system.
 */
public class EFPlaceholderService implements PlaceholderService
{
    private final EagleFactions plugin;

    private final Map<Placeholder, EFPlaceholderParser> placeholderParsers = new EnumMap<>(Placeholder.class);

    public EFPlaceholderService(EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    private void initDefaultParsers()
    {
        placeholderParsers.put(Placeholder.FACTION_NAME, new FactionNameParser(this.plugin.getFactionLogic()));
        placeholderParsers.put(Placeholder.FACTION_TAG, new FactionTagParser(this.plugin.getFactionLogic()));
        placeholderParsers.put(Placeholder.FACTION_TAG_WITH_BRACKETS, new FactionTagWithBracketsParser(this.plugin.getFactionLogic(), this.plugin.getConfiguration().getChatConfig()));
        placeholderParsers.put(Placeholder.FACTION_POWER, new FactionPowerParser(this.plugin.getFactionLogic(), this.plugin.getPowerManager()));
        placeholderParsers.put(Placeholder.FACTION_MAX_POWER, new FactionMaxPowerParser(this.plugin.getFactionLogic(), this.plugin.getPowerManager()));
        placeholderParsers.put(Placeholder.FACTION_LAST_ONLINE, new FactionLastOnlineParser(this.plugin.getFactionLogic()));
        placeholderParsers.put(Placeholder.FACTION_CLAIMS_COUNT, new FactionClaimsCountParser(this.plugin.getFactionLogic()));
        placeholderParsers.put(Placeholder.FACTION_MEMBERS_COUNT, new FactionMembersCountParser(this.plugin.getFactionLogic()));
        placeholderParsers.put(Placeholder.FACTION_ALLIANCES, new FactionAlliancesParser(this.plugin.getFactionLogic()));
        placeholderParsers.put(Placeholder.FACTION_ENEMIES, new FactionEnemiesParser(this.plugin.getFactionLogic()));
        placeholderParsers.put(Placeholder.FACTION_TRUCES, new FactionTrucesParser(this.plugin.getFactionLogic()));

        placeholderParsers.put(Placeholder.PLAYER_POWER, new PlayerPowerParser(this.plugin.getPlayerManager()));
        placeholderParsers.put(Placeholder.PLAYER_MAX_POWER, new PlayerMaxPowerParser(this.plugin.getPlayerManager()));
        placeholderParsers.put(Placeholder.PLAYER_LAST_ONLINE, new PlayerLastOnlineParser());
    }

    public void onRegisterPlaceholderEvent(RegisterRegistryValueEvent.GameScoped event)
    {
        initDefaultParsers();

        //Register placeholders
        RegisterRegistryValueEvent.RegistryStep<PlaceholderParser> placeholderParserRegistryStep = event.registry(RegistryTypes.PLACEHOLDER_PARSER);
        for (Map.Entry<Placeholder, EFPlaceholderParser> entry : this.placeholderParsers.entrySet())
        {
            placeholderParserRegistryStep.register(ResourceKey.of(PluginInfo.ID, entry.getKey().getName()), (context) -> processSpongePlaceholderContext(context, entry.getKey()));
        }
    }

    @Override
    public Optional<String> resolvePlaceholder(ServerPlayer serverPlayer, Placeholder placeholder)
    {
        Map<Class<?>, Object> params = new HashMap<>();
        params.put(ServerPlayer.class, serverPlayer);

        return placeholderParsers.get(placeholder).parse(new ParsingContext(Optional.empty(), params));
    }

    public Component processSpongePlaceholderContext(final PlaceholderContext placeholderContext, Placeholder placeholder)
    {
        ServerPlayer serverPlayer = placeholderContext.associatedObject()
            .filter(ServerPlayer.class::isInstance)
            .map(ServerPlayer.class::cast)
            .orElse(null);

        Faction faction = placeholderContext.associatedObject()
                .filter(Faction.class::isInstance)
                .map(Faction.class::cast)
                .orElse(null);

        Map<Class<?>, Object> params = new HashMap<>();
        params.put(ServerPlayer.class, serverPlayer);
        params.put(Faction.class, faction);

        ParsingContext parsingContext = new ParsingContext(placeholderContext.argumentString(), params);
        return placeholderParsers.get(placeholder).parse(parsingContext)
                .map(Component::text)
                .orElse(Component.empty());
    }
}
