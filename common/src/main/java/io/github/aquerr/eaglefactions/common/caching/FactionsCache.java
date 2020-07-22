package io.github.aquerr.eaglefactions.common.caching;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FactionsCache
{
    private static final Map<String, Faction> FACTIONS_CACHE = new HashMap<>();
    private static final Set<Claim> CLAIMS_CACHE = new HashSet<>();
    private static final Map<UUID, FactionPlayer> FACTION_PLAYER_CACHE = new HashMap<>();

    // TODO: Add cache time to configuration?
    private static final Cache<Claim, Optional<Faction>> cachedChunks = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private FactionsCache()
    {

    }

    public static Optional<Faction> getClaimFaction(Claim claim) {
        return cachedChunks.getIfPresent(claim);
    }

    public static void updateClaimFaction(Claim claim, Optional<Faction> faction) {
        cachedChunks.put(claim, faction);
    }

    public static Map<UUID, FactionPlayer> getPlayersMap()
    {
        return FACTION_PLAYER_CACHE;
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
                CLAIMS_CACHE.removeAll(factionToUpdate.getClaims());
            }
            else
            {
                FACTIONS_CACHE.put(faction.getName().toLowerCase(), faction);
            }

            if(!faction.getClaims().isEmpty())
            {
                CLAIMS_CACHE.addAll(faction.getClaims());
            }
        }
    }

    public static void removeFaction(final String factionName)
    {
        synchronized (FACTIONS_CACHE)
        {
            Faction faction = FACTIONS_CACHE.remove(factionName.toLowerCase());
            CLAIMS_CACHE.removeAll(faction.getClaims());
        }
    }

    @Nullable
    public static Faction getFaction(final String factionName)
    {
        Faction optionalFaction = FACTIONS_CACHE.get(factionName.toLowerCase());

        if (optionalFaction != null)
        {
            return optionalFaction;
        }

        return null;
    }

    public static Set<Claim> getClaims()
    {
        return CLAIMS_CACHE;
    }

    public static void removeClaim(final Claim claim)
    {
        CLAIMS_CACHE.remove(claim);
    }

    public static void clear()
    {
        CLAIMS_CACHE.clear();
        FACTIONS_CACHE.clear();
        FACTION_PLAYER_CACHE.clear();
    }
}
