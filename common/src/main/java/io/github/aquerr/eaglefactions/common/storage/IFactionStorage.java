package io.github.aquerr.eaglefactions.common.storage;

import io.github.aquerr.eaglefactions.entities.Faction;

import java.util.Set;

public interface IFactionStorage
{
    boolean addOrUpdateFaction(Faction faction);

    Faction getFaction(String factionName);

    Set<Faction> getFactions();

    void load();

    boolean deleteFaction(String factionName);
}
