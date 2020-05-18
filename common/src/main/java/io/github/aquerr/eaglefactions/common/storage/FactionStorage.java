package io.github.aquerr.eaglefactions.common.storage;

import io.github.aquerr.eaglefactions.api.entities.Faction;

import java.util.Set;

public interface FactionStorage
{
    boolean saveFaction(Faction faction);

    Faction getFaction(String factionName);

    Set<Faction> getFactions();

    void load();

    boolean deleteFaction(String factionName);

    void deleteFactions();
}
