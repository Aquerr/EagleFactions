package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.Faction;

import java.util.Map;

public interface IFactionStorage
{
    boolean addOrUpdateFaction(Faction faction);

    Faction getFaction(String factionName);

    void load();

    boolean queueRemoveFaction(String factionName);
}
