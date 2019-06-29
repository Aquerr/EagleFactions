package io.github.aquerr.eaglefactions.storage.utils;

public class DeleteFactionTask implements IStorageTask
{
    private final String factionName;

    public DeleteFactionTask(String factionName)
    {
        this.factionName = factionName;
    }

    public String getFactionName()
    {
        return this.factionName;
    }
}
