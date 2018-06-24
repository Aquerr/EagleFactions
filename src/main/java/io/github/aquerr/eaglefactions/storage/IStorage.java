package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.Faction;

public interface IStorage
{
    boolean addOrUpdateFaction(Faction faction);

    boolean removeFaction(String factionName);

    Faction getFaction(String factionName);

    boolean saveChanges();

    void load();
}
