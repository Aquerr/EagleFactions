package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.storage.h2.H2FactionStorage;
import io.github.aquerr.eaglefactions.storage.h2.H2PlayerStorage;
import io.github.aquerr.eaglefactions.storage.hocon.HOCONFactionStorage;
import io.github.aquerr.eaglefactions.storage.hocon.HOCONPlayerStorage;
import io.github.aquerr.eaglefactions.storage.mysql.MySQLFactionStorage;
import io.github.aquerr.eaglefactions.storage.mysql.MySQLPlayerStorage;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class StorageManager
{
    private static StorageManager INSTANCE = null;

    private final IFactionStorage factionsStorage;
    private final IPlayerStorage playerStorage;
    private final Queue<IStorageTask> storageTaskQueue;
    private final EagleFactions plugin;

    private final Thread storageThread;

    public static StorageManager getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new StorageManager(eagleFactions);
        else return INSTANCE;
    }

    private StorageManager(EagleFactions eagleFactions)
    {
        this.plugin = eagleFactions;
        ConfigFields configFields = eagleFactions.getConfiguration().getConfigFields();
        Path configDir = eagleFactions.getConfigDir();
        switch(configFields.getStorageType().toLowerCase())
        {
            case "hocon":
                factionsStorage = new HOCONFactionStorage(configDir);
                playerStorage = new HOCONPlayerStorage(configDir);
                this.plugin.printInfo("HOCON storage has been initialized!");
                break;
            case "h2":
                factionsStorage = new H2FactionStorage(eagleFactions);
                playerStorage = new H2PlayerStorage(eagleFactions);
                this.plugin.printInfo("H2 storage has been initialized!");
                break;
                //TODO: Add SQLLite, JSON, etc...
//            case "sqllite":
//
//                break;
            case "mysql":
                factionsStorage = new MySQLFactionStorage(eagleFactions);
                playerStorage = new MySQLPlayerStorage(eagleFactions);
                this.plugin.printInfo("MySQL storage has been initialized!");
                break;
            default: //HOCON
                this.plugin.printInfo("Couldn't find provided storage type.");
                factionsStorage = new HOCONFactionStorage(configDir);
                playerStorage = new HOCONPlayerStorage(configDir);
                this.plugin.printInfo("Initialized default HOCON storage.");
                break;
        }
        prepareFactionsCache();
//        preparePlayerCache();

        this.storageTaskQueue = new LinkedList<>();
        this.storageThread = new Thread(run());
        this.storageThread.start();
    }

    private void queueStorageTask(IStorageTask task)
    {
        this.storageTaskQueue.add(task);
    }

    public Runnable run()
    {
        return () ->
        {
            while(true)
            {
                if(storageTaskQueue.size() > 0)
                {
                    synchronized(storageTaskQueue)
                    {
                        IStorageTask storageTask = storageTaskQueue.poll();
                        if (storageTask instanceof DeleteFactionTask)
                            factionsStorage.deleteFaction(((DeleteFactionTask) storageTask).getFactionName());
                        else if (storageTask instanceof UpdateFactionTask)
                            factionsStorage.addOrUpdateFaction(((UpdateFactionTask) storageTask).getFaction());
                    }
                }
                else
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public void addOrUpdateFaction(Faction faction)
    {
        FactionsCache.addOrUpdateFactionCache(faction);
        queueStorageTask(new UpdateFactionTask(faction));
    }

    public boolean deleteFaction(String factionName)
    {
        FactionsCache.removeFactionCache(factionName);
        queueStorageTask(new DeleteFactionTask(factionName));
        return true;
    }

    public @Nullable Faction getFaction(String factionName)
    {
        try
        {
            Faction factionCache = FactionsCache.getFactionCache(factionName);
            if(factionCache != null)
                return factionCache;

            Faction faction = this.factionsStorage.getFaction(factionName);
            if (faction == null)
                return null;

            FactionsCache.addOrUpdateFactionCache(faction);

            return faction;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        //If it was not possible to get a faction then return null.
        return null;
    }

    private void prepareFactionsCache()
    {
        final Set<Faction> factionSet = this.factionsStorage.getFactions();
        for (Faction faction : factionSet)
        {
            FactionsCache.addOrUpdateFactionCache(faction);
        }
    }

    public void reloadStorage()
    {
        FactionsCache.clearCache();
        this.factionsStorage.load();
        prepareFactionsCache();
    }

    public boolean checkIfPlayerExists(final UUID playerUUID, final String playerName)
    {
        return this.playerStorage.checkIfPlayerExists(playerUUID, playerName);
    }

    public boolean addPlayer(final UUID playerUUID, final String playerName, final float startingPower, final float globalMaxPower)
    {
        return this.playerStorage.addPlayer(playerUUID, playerName, startingPower, globalMaxPower);
    }

    public boolean setDeathInWarzone(final UUID playerUUID, final boolean didDieInWarZone)
    {
        return this.playerStorage.setDeathInWarzone(playerUUID, didDieInWarZone);
    }

    public boolean getLastDeathInWarzone(final UUID playerUUID)
    {
        return this.playerStorage.getLastDeathInWarzone(playerUUID);
    }

    public float getPlayerPower(final UUID playerUUID)
    {
        return this.playerStorage.getPlayerPower(playerUUID);
    }

    public boolean setPlayerPower(final UUID playerUUID, final float power)
    {
        return this.playerStorage.setPlayerPower(playerUUID, power);
    }

    public float getPlayerMaxPower(final UUID playerUUID)
    {
        return this.playerStorage.getPlayerMaxPower(playerUUID);
    }

    public boolean setPlayerMaxPower(final UUID playerUUID, final float maxpower)
    {
        return this.playerStorage.setPlayerMaxPower(playerUUID, maxpower);
    }

    public Set<String> getServerPlayerNames()
    {
        return this.playerStorage.getServerPlayerNames();
    }

    public Set<IFactionPlayer> getServerPlayers()
    {
        return this.playerStorage.getServerPlayers();
    }

    public String getPlayerName(final UUID playerUUID)
    {
        return this.playerStorage.getPlayerName(playerUUID);
    }

    public boolean updatePlayerName(final UUID playerUUID, final String playerName)
    {
        return this.playerStorage.updatePlayerName(playerUUID, playerName);
    }
}
