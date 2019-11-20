package io.github.aquerr.eaglefactions.common.storage;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.storage.sql.h2.H2FactionStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.h2.H2PlayerStorage;
import io.github.aquerr.eaglefactions.common.storage.file.hocon.HOCONFactionStorage;
import io.github.aquerr.eaglefactions.common.storage.file.hocon.HOCONPlayerStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.mariadb.MariaDbFactionStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.mariadb.MariaDbPlayerStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.mysql.MySQLFactionStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.mysql.MySQLPlayerStorage;
import io.github.aquerr.eaglefactions.common.storage.utils.DeleteFactionTask;
import io.github.aquerr.eaglefactions.common.storage.utils.IStorageTask;
import io.github.aquerr.eaglefactions.common.storage.utils.UpdateFactionTask;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StorageManagerImpl implements StorageManager, Runnable
{
    private static StorageManagerImpl INSTANCE = null;

    private final IFactionStorage factionsStorage;
    private final IPlayerStorage playerStorage;
    private final Queue<IStorageTask> storageTaskQueue;
    private final EagleFactions plugin;

    private final Thread storageThread;

    public static StorageManagerImpl getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new StorageManagerImpl(eagleFactions);
        else return INSTANCE;
    }

    private StorageManagerImpl(final EagleFactions eagleFactions)
    {
        INSTANCE = this;
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
            case "mysql":
                factionsStorage = new MySQLFactionStorage(eagleFactions);
                playerStorage = new MySQLPlayerStorage(eagleFactions);
                this.plugin.printInfo("MySQL storage has been initialized!");
                break;
            case "mariadb":
                factionsStorage = new MariaDbFactionStorage(eagleFactions);
                playerStorage = new MariaDbPlayerStorage(eagleFactions);
                this.plugin.printInfo("MariaDB storage has been initialized!");
                break;
            default: //HOCON
                this.plugin.printInfo("Couldn't find provided storage type.");
                factionsStorage = new HOCONFactionStorage(configDir);
                playerStorage = new HOCONPlayerStorage(configDir);
                this.plugin.printInfo("Initialized default HOCON storage.");
                break;
        }
        prepareFactionsCache();

        this.storageTaskQueue = new LinkedList<>();
        this.storageThread = new Thread(this::run);
        this.storageThread.start();
    }

    private void queueStorageTask(IStorageTask task)
    {
        synchronized(this.storageTaskQueue)
        {
            this.storageTaskQueue.add(task);
            this.storageTaskQueue.notify();
        }
    }

    @Override
    public void run()
    {
        while(true)
        {
            synchronized(this.storageTaskQueue)
            {
                if(storageTaskQueue.size() > 0)
                {
                    IStorageTask storageTask = storageTaskQueue.poll();
                    if (storageTask instanceof DeleteFactionTask)
                        factionsStorage.deleteFaction(((DeleteFactionTask) storageTask).getFactionName());
                    else if (storageTask instanceof UpdateFactionTask)
                        factionsStorage.addOrUpdateFaction(((UpdateFactionTask) storageTask).getFaction());
                }
                else
                {
                    try
                    {
                        this.storageTaskQueue.wait();
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void addOrUpdateFaction(final Faction faction)
    {
        FactionsCache.addOrUpdateFactionCache(faction);
        queueStorageTask(new UpdateFactionTask(faction));
    }

    @Override
    public boolean deleteFaction(final String factionName)
    {
        FactionsCache.removeFactionCache(factionName);
        queueStorageTask(new DeleteFactionTask(factionName));
        return true;
    }

    @Override
    public @Nullable
    Faction getFaction(final String factionName)
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
        for (final Faction faction : factionSet)
        {
            FactionsCache.addOrUpdateFactionCache(faction);
        }
    }

    @Override
    public void reloadStorage()
    {
        FactionsCache.clearCache();
        this.factionsStorage.load();
        prepareFactionsCache();
    }

    @Override
    public boolean checkIfPlayerExists(final UUID playerUUID, final String playerName)
    {
        return this.playerStorage.checkIfPlayerExists(playerUUID, playerName);
    }

    @Override
    public boolean addPlayer(final UUID playerUUID, final String playerName, final float startingPower, final float maxpower)
    {
        return CompletableFuture.supplyAsync(() -> playerStorage.addPlayer(playerUUID, playerName, startingPower, maxpower)).isDone();
        //return this.playerStorage.addPlayer(playerUUID, playerName, startingPower, globalMaxPower);
    }

    @Override
    public boolean setDeathInWarzone(final UUID playerUUID, final boolean didDieInWarZone)
    {
        return CompletableFuture.supplyAsync(() -> this.playerStorage.setDeathInWarzone(playerUUID, didDieInWarZone)).isDone();
        //return this.playerStorage.setDeathInWarzone(playerUUID, didDieInWarZone);
    }

    @Override
    public boolean getLastDeathInWarzone(final UUID playerUUID)
    {
        return this.playerStorage.getLastDeathInWarzone(playerUUID);
    }

    @Override
    public float getPlayerPower(final UUID playerUUID)
    {
        return this.playerStorage.getPlayerPower(playerUUID);
    }

    @Override
    public boolean setPlayerPower(final UUID playerUUID, final float power)
    {
        return CompletableFuture.supplyAsync(() -> this.playerStorage.setPlayerPower(playerUUID, power)).isDone();
    }

    @Override
    public float getPlayerMaxPower(final UUID playerUUID)
    {
        return this.playerStorage.getPlayerMaxPower(playerUUID);
    }

    @Override
    public boolean setPlayerMaxPower(final UUID playerUUID, final float maxpower)
    {
        return CompletableFuture.supplyAsync(()-> this.playerStorage.setPlayerMaxPower(playerUUID, maxpower)).isDone();
    }

    @Override
    public Set<String> getServerPlayerNames()
    {
        return this.playerStorage.getServerPlayerNames();
    }

    @Override
    public Set<FactionPlayer> getServerPlayers()
    {
        return this.playerStorage.getServerPlayers();
    }

    @Override
    public String getPlayerName(final UUID playerUUID)
    {
        return this.playerStorage.getPlayerName(playerUUID);
    }

    @Override
    public boolean updatePlayerName(final UUID playerUUID, final String playerName)
    {
        return CompletableFuture.supplyAsync(()->this.playerStorage.updatePlayerName(playerUUID, playerName)).isDone();
    }
}
