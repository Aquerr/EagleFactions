package io.github.aquerr.eaglefactions.caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FactionsCache
{
    private static final Map<String, Faction> FACTIONS_CACHE = new HashMap<>();
    private static final Map<UUID, FactionPlayer> FACTION_PLAYER_CACHE = new HashMap<>();

    private static final Cache<Claim, Faction> CLAIMS_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build();

    private FactionsCache()
    {

    }

    public static Optional<Faction> getClaimFaction(Claim claim) {
        return Optional.ofNullable(CLAIMS_CACHE.getIfPresent(claim));
    }

    public static void updateClaimFaction(Claim claim, Faction faction) {
        CLAIMS_CACHE.put(claim, faction);
    }

    public static Set<FactionPlayer> getPlayers()
    {
        return Collections.unmodifiableSet(new HashSet<>(FACTION_PLAYER_CACHE.values()));
    }

    public static void savePlayer(final FactionPlayer factionPlayer)
    {
        synchronized (FACTION_PLAYER_CACHE)
        {
            FACTION_PLAYER_CACHE.put(factionPlayer.getUniqueId(), factionPlayer);
        }
    }

    public static @Nullable FactionPlayer getPlayer(final UUID playerUniqueId)
    {
        synchronized (FACTION_PLAYER_CACHE)
        {
            return FACTION_PLAYER_CACHE.get(playerUniqueId);
        }
    }

    public static void removePlayer(final UUID playerUniqueId)
    {
        synchronized (FACTION_PLAYER_CACHE)
        {
            FACTION_PLAYER_CACHE.remove(playerUniqueId);
        }
    }

    public static Map<String, Faction> getFactionsMap()
    {
        return Collections.unmodifiableMap(FACTIONS_CACHE);
    }

    public static void saveFaction(final Faction faction)
    {
        synchronized (FACTIONS_CACHE)
        {
            Faction factionToUpdate = FACTIONS_CACHE.get(faction.getName().toLowerCase());

            if (factionToUpdate != null)
            {
                FACTIONS_CACHE.replace(factionToUpdate.getName().toLowerCase(), faction);
                factionToUpdate.getClaims().forEach(CLAIMS_CACHE::invalidate);
            }
            else
            {
                FACTIONS_CACHE.put(faction.getName().toLowerCase(), faction);
            }

            if(!faction.getClaims().isEmpty())
            {
                faction.getClaims().forEach(claim -> CLAIMS_CACHE.put(claim, faction));
            }
        }
    }

    public static void removeFaction(final String factionName)
    {
        synchronized (FACTIONS_CACHE)
        {
            Faction faction = FACTIONS_CACHE.remove(factionName.toLowerCase());
            faction.getClaims().forEach(CLAIMS_CACHE::invalidate);
        }
    }

    @Nullable
    public static Faction getFaction(final String factionName)
    {
        return FACTIONS_CACHE.get(factionName.toLowerCase());
    }

    public static Map<Claim, Faction> getClaims()
    {
        return Collections.unmodifiableMap(CLAIMS_CACHE.asMap());
    }

    public static void removeClaim(final Claim claim)
    {
        CLAIMS_CACHE.invalidate(claim);
    }

    public static void clear()
    {
        CLAIMS_CACHE.invalidateAll();
        FACTIONS_CACHE.clear();
        FACTION_PLAYER_CACHE.clear();
    }
}
