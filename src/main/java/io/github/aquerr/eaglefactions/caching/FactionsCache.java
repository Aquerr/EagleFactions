package io.github.aquerr.eaglefactions.caching;

import io.github.aquerr.eaglefactions.entities.Faction;

import javax.annotation.Nullable;
import java.util.*;

public class FactionsCache
{
    private static Map<String, Faction> factions = new HashMap<>();

    private FactionsCache()
    {

    }

    public static Map<String, Faction> getFactionsMap()
    {
        return factions;
    }

    public static void addOrUpdateFactionCache(Faction faction)
    {
        Faction factionToUpdate = factions.get(faction.Name.toLowerCase());

        if (factionToUpdate != null)
        {
            factions.remove(factionToUpdate.Name.toLowerCase());
            factions.put(faction.Name.toLowerCase(), faction);
        }
        else
        {
            factions.put(faction.Name.toLowerCase(), faction);
        }
    }

    public static void removeFactionCache(String factionName)
    {
        Faction factionToRemove = factions.get(factionName.toLowerCase());

        if (factionToRemove != null)
        {
            factions.remove(factionToRemove.Name.toLowerCase());
        }
    }

    public static @Nullable Faction getFactionCache(String factionName)
    {
        Faction optionalFaction = factions.get(factionName.toLowerCase());

        if (optionalFaction != null)
        {
            return optionalFaction;
        }

        return null;
    }
}
