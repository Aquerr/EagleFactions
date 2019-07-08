package io.github.aquerr.eaglefactions.common.storage.utils;

import io.github.aquerr.eaglefactions.api.entities.Faction;

public class UpdateFactionTask implements IStorageTask
{
    private final Faction faction;

    public UpdateFactionTask(Faction faction)
    {
        this.faction = faction;
    }

    public Faction getFaction()
    {
        return this.faction;
    }
}
