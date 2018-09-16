package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.Faction;

import java.util.Map;

public interface IFactionStorage
{
    boolean addOrUpdateFaction(Faction faction);

    boolean renameFaction(Faction faction, String newName);

    boolean removeFaction(String factionName);

    Faction getFaction(String factionName);

    Map<String, Faction> getFactionsMap();

    void load();
}
