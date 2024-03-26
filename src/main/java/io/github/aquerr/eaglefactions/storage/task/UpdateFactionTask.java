package io.github.aquerr.eaglefactions.storage.task;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.storage.FactionStorage;

import java.util.concurrent.CompletionException;

public class UpdateFactionTask implements IStorageTask
{
    private final FactionStorage factionStorage;
    private final Faction faction;

    public UpdateFactionTask(FactionStorage factionStorage, final Faction faction)
    {
        this.factionStorage = factionStorage;
        this.faction = faction;
    }

    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public void run()
    {
        try
        {
            this.factionStorage.saveFaction(faction);
        }
        catch (Exception exception)
        {
            throw new CompletionException("Could not save/update faction: " + faction.getName(), exception);
        }
    }
}
