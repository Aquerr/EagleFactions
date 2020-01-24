package io.github.aquerr.eaglefactions.common.storage.utils;

import io.github.aquerr.eaglefactions.api.entities.Faction;

public class UpdateFactionTask implements IStorageTask
{
    private final Faction faction;
    private final Runnable runnable;

    public UpdateFactionTask(final Faction faction, final Runnable runnable)
    {
        this.faction = faction;
        this.runnable = runnable;
    }

    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public void run()
    {
        this.runnable.run();
    }
}
