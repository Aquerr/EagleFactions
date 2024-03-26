package io.github.aquerr.eaglefactions.storage.task;

import io.github.aquerr.eaglefactions.storage.FactionStorage;

import java.util.concurrent.CompletionException;

public class DeleteFactionTask implements IStorageTask
{
    private final FactionStorage factionStorage;
    private final String factionName;

    public DeleteFactionTask(FactionStorage factionStorage, String factionName)
    {
        this.factionStorage = factionStorage;
        this.factionName = factionName;
    }

    public String getFactionName()
    {
        return this.factionName;
    }

    @Override
    public void run()
    {
        try
        {
            this.factionStorage.deleteFaction(factionName);
        }
        catch (Exception exception)
        {
            throw new CompletionException("Could not delete faction: " + factionName, exception);
        }
    }
}
