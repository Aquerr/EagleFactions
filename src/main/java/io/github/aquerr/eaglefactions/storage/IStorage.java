package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.Faction;

import java.util.List;

public interface IStorage
{
    boolean addOrUpdateFaction(Faction faction);

    boolean removeFaction(String factionName);

    Faction getFaction(String factionName);

    List<Faction> getFactions();

    void load();
}
