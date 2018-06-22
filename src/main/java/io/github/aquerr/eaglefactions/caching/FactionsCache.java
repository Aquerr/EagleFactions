package io.github.aquerr.eaglefactions.caching;

import io.github.aquerr.eaglefactions.entities.Faction;

import javax.annotation.Nullable;
import java.util.*;

public class FactionsCache
{
    private static Map<String, Faction> factionsCacheMap = new HashMap<>();
    private static Set<String> claimsCacheList = new HashSet<>();

    private FactionsCache()
    {

    }

    public static Map<String, Faction> getFactionsMap()
    {
        return factionsCacheMap;
    }

    public static void addOrUpdateFactionCache(Faction faction)
    {
        Faction factionToUpdate = factionsCacheMap.get(faction.Name.toLowerCase());

        if (factionToUpdate != null)
        {
            factionsCacheMap.replace(factionToUpdate.Name.toLowerCase(), faction);

            for(String claim : factionToUpdate.Claims)
            {
                claimsCacheList.remove(claim);
            }
        }
        else
        {
            factionsCacheMap.put(faction.Name.toLowerCase(), faction);
        }

        if(!faction.Claims.isEmpty())
        {
            claimsCacheList.addAll(faction.Claims);
        }
    }

    public static void removeFactionCache(String factionName)
    {
        Faction faction = factionsCacheMap.get(factionName.toLowerCase());
        factionsCacheMap.remove(factionName.toLowerCase());

        for(String claim : faction.Claims)
        {
            claimsCacheList.remove(claim);
        }
    }

    @Nullable
    public static Faction getFactionCache(String factionName)
    {
        Faction optionalFaction = factionsCacheMap.get(factionName.toLowerCase());

        if (optionalFaction != null)
        {
            return optionalFaction;
        }

        return null;
    }

    public static Set<String> getAllClaims()
    {
        return claimsCacheList;
    }
}
