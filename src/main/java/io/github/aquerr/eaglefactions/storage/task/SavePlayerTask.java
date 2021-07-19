package io.github.aquerr.eaglefactions.storage.task;

import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;

public class SavePlayerTask implements IStorageTask
{
    private final FactionPlayer factionPlayer;
    private final Runnable runnable;

    public SavePlayerTask(FactionPlayer factionPlayer, Runnable runnable)
    {
        this.factionPlayer = factionPlayer;
        this.runnable = runnable;
    }

    @Override
    public void run()
    {
        this.runnable.run();
    }
}
