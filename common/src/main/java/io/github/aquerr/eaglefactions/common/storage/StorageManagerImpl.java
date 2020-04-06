package io.github.aquerr.eaglefactions.common.storage;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.StorageConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
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
import io.github.aquerr.eaglefactions.common.storage.util.DeleteFactionTask;
import io.github.aquerr.eaglefactions.common.storage.util.IStorageTask;
import io.github.aquerr.eaglefactions.common.storage.util.UpdateFactionTask;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class StorageManagerImpl implements StorageManager
{
    private final FactionStorage factionsStorage;
    private final PlayerStorage playerStorage;
    private final BackupStorage backupStorage;
    private final Path configDir;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public StorageManagerImpl(final EagleFactions plugin, final StorageConfig storageConfig, final Path configDir)
    {
        this.configDir = configDir;
        switch(storageConfig.getStorageType().toLowerCase())
        {
            case "hocon":
                factionsStorage = new HOCONFactionStorage(configDir);
                playerStorage = new HOCONPlayerStorage(configDir);
                plugin.printInfo("HOCON storage has been initialized!");
                break;
            case "h2":
                factionsStorage = new H2FactionStorage(plugin);
                playerStorage = new H2PlayerStorage(plugin);
                plugin.printInfo("H2 storage has been initialized!");
                break;
            case "mysql":
                factionsStorage = new MySQLFactionStorage(plugin);
                playerStorage = new MySQLPlayerStorage(plugin);
                plugin.printInfo("MySQL storage has been initialized!");
                break;
            case "mariadb":
                factionsStorage = new MariaDbFactionStorage(plugin);
                playerStorage = new MariaDbPlayerStorage(plugin);
                plugin.printInfo("MariaDB storage has been initialized!");
                break;
            default: //HOCON
                plugin.printInfo("Couldn't find provided storage type.");
                factionsStorage = new HOCONFactionStorage(configDir);
                playerStorage = new HOCONPlayerStorage(configDir);
                plugin.printInfo("Initialized default HOCON storage.");
                break;
        }
        this.backupStorage = new BackupStorage(factionsStorage, playerStorage, configDir);
        prepareFactionsCache();
    }

    private void queueStorageTask(IStorageTask task)
    {
        this.executorService.execute(task);
    }

    @Override
    public void addOrUpdateFaction(final Faction faction)
    {
        FactionsCache.addOrUpdateFactionCache(faction);
        queueStorageTask(new UpdateFactionTask(faction, () -> this.factionsStorage.addOrUpdateFaction(faction)));
    }

    @Override
    public boolean deleteFaction(final String factionName)
    {
        FactionsCache.removeFactionCache(factionName);
        queueStorageTask(new DeleteFactionTask(factionName, () -> this.factionsStorage.deleteFaction(factionName)));
        return true;
    }

    @Override
    public @Nullable Faction getFaction(final String factionName)
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

    @Override
    public boolean createBackup()
    {
        return this.backupStorage.createBackup();
    }

    @Override
    public boolean restoreBackup(final String backupName)
    {
        try
        {
            return this.backupStorage.restoreBackup(backupName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> listBackups()
    {
        return this.backupStorage.listBackups();
    }
}
