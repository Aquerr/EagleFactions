package io.github.aquerr.eaglefactions.storage.task;

import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.storage.PlayerStorage;

public class SavePlayerTask implements IStorageTask
{
    private final PlayerStorage playerStorage;
    private final FactionPlayer factionPlayer;

    public SavePlayerTask(PlayerStorage playerStorage, FactionPlayer factionPlayer)
    {
        this.playerStorage = playerStorage;
        this.factionPlayer = factionPlayer;
    }

    @Override
    public void run()
    {
        this.playerStorage.savePlayer(factionPlayer);
    }
}
