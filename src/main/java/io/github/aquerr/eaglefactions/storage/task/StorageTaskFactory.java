package io.github.aquerr.eaglefactions.storage.task;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.storage.FactionStorage;
import io.github.aquerr.eaglefactions.storage.PlayerStorage;

public class StorageTaskFactory
{
    private final PlayerStorage playerStorage;
    private final FactionStorage factionStorage;

    public StorageTaskFactory(PlayerStorage playerStorage, FactionStorage factionStorage)
    {
        this.playerStorage = playerStorage;
        this.factionStorage = factionStorage;
    }

    public IStorageTask deleteFaction(String factionName)
    {
        return new DeleteFactionTask(factionStorage, factionName);
    }

    public IStorageTask saveFaction(Faction faction)
    {
        return new UpdateFactionTask(factionStorage, faction);
    }

    public IStorageTask savePlayer(FactionPlayer factionPlayer)
    {
        return new SavePlayerTask(playerStorage, factionPlayer);
    }
}
