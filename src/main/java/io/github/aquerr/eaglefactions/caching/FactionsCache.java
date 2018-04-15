package io.github.aquerr.eaglefactions.caching;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.managers.PowerManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Singleton
public class FactionsCache
{
    private static List<Faction> _factionsList = new ArrayList<>();

    private FactionsCache()
    {

    }

    public static List<Faction> getFactionsList()
    {
        return _factionsList;
    }

    public static void addOrUpdateFactionCache(Faction faction)
    {
        Optional<Faction> optionalFaction = _factionsList.stream().filter(x->x.Name == faction.Name).findFirst();

        if (optionalFaction.isPresent())
        {
            Faction factionToUpdate = optionalFaction.get();
            _factionsList.remove(factionToUpdate);
            _factionsList.add(faction);
        }
        else
        {
            _factionsList.add(faction);
        }
    }

    public static void removeFactionCache(String factionName)
    {
        Optional<Faction> optionalFaction = _factionsList.stream().filter(x->x.Name == factionName).findFirst();

        if (optionalFaction.isPresent())
        {
            Faction factionToRemove = optionalFaction.get();
            _factionsList.remove(factionToRemove);
        }
    }

    public static @Nullable Faction getFactionCache(String factionName)
    {
        Optional<Faction> optionalFaction = _factionsList.stream().filter(x->x.Name == factionName).findFirst();

        if (optionalFaction.isPresent())
        {
            Faction faction = optionalFaction.get();

            //Update power and cache
            faction.Power = PowerManager.getFactionPower(faction);
            _factionsList.remove(faction);
            _factionsList.add(faction);

            //Return faction
            return faction;
        }

        return null;
    }
}
