package io.github.aquerr.eaglefactions.caching;

import io.github.aquerr.eaglefactions.entities.Faction;

import javax.annotation.Nullable;
import java.util.*;

public class FactionsCache
{
    //TODO: Add a second thread for saving factions' data.

    private static Map<String, Faction> factionsCacheMap = new HashMap<>();
    private static Set<String> claimsCacheSet = new HashSet<>();

    private FactionsCache()
    {

    }

    public static Map<String, Faction> getFactionsMap()
    {
        return factionsCacheMap;
    }

    public static void addOrUpdateFactionCache(Faction faction)
    {
        Faction factionToUpdate = factionsCacheMap.get(faction.getName().toLowerCase());

        if (factionToUpdate != null)
        {
            factionsCacheMap.replace(factionToUpdate.getName().toLowerCase(), faction);

            for(String claim : factionToUpdate.getClaims())
            {
                claimsCacheSet.remove(claim);
            }
        }
        else
        {
            factionsCacheMap.put(faction.getName().toLowerCase(), faction);
        }

        if(!faction.getClaims().isEmpty())
        {
            claimsCacheSet.addAll(faction.getClaims());
        }
    }

    public static void removeFactionCache(String factionName)
    {
        Faction faction = factionsCacheMap.get(factionName.toLowerCase());
        factionsCacheMap.remove(factionName.toLowerCase());

        for(String claim : faction.getClaims())
        {
            claimsCacheSet.remove(claim);
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
        return claimsCacheSet;
    }
}
