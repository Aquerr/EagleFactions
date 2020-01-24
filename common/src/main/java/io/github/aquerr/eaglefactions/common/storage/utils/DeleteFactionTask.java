package io.github.aquerr.eaglefactions.common.storage.utils;

public class DeleteFactionTask implements IStorageTask
{
    private final String factionName;
    private final Runnable runnable;

    public DeleteFactionTask(String factionName, Runnable runnable)
    {
        this.factionName = factionName;
        this.runnable = runnable;
    }

    public String getFactionName()
    {
        return this.factionName;
    }

    @Override
    public void run()
    {
        this.runnable.run();
    }
}
