package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.StorageConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.storage.file.hocon.HOCONFactionStorage;
import io.github.aquerr.eaglefactions.storage.file.hocon.HOCONPlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.DatabaseInitializer;
import io.github.aquerr.eaglefactions.storage.sql.DatabaseProperties;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.h2.H2ConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.h2.H2FactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.h2.H2PlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.mariadb.MariaDbConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.mariadb.MariaDbFactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.mariadb.MariaDbPlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.mysql.MySQLConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.mysql.MySQLFactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.mysql.MySQLPlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.sqlite.SqliteConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.sqlite.SqliteFactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.sqlite.SqlitePlayerStorage;
import io.github.aquerr.eaglefactions.storage.task.IStorageTask;
import io.github.aquerr.eaglefactions.storage.task.StorageTaskFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

public class StorageManagerImpl implements StorageManager
{
    private final FactionStorage factionStorage;
    private final PlayerStorage playerStorage;
    private final BackupStorage backupStorage;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); //Only one thread.

    private final StorageTaskFactory storageTaskFactory;

    public StorageManagerImpl(final EagleFactions plugin, final StorageConfig storageConfig, final Path configDir)
    {
        StorageType storageType = StorageType.findByName(storageConfig.getStorageType().toLowerCase()).orElse(null);
        if (storageType == null)
        {
            throw new IllegalArgumentException(format("Storage type '%s' has not been recognized!", storageConfig.getStorageType()));
        }

        SQLConnectionProvider sqlConnectionProvider = null;
        switch(storageType)
        {

            case H2:
            {
                DatabaseProperties databaseProperties = prepareDatabaseProperties(storageConfig);
                databaseProperties.setDatabaseFileDirectory(plugin.getConfigDir().resolve("data/h2"));
                sqlConnectionProvider = new H2ConnectionProvider(databaseProperties);
                factionStorage = new H2FactionStorage(EagleFactionsPlugin.getPlugin().getLogger(), (H2ConnectionProvider) sqlConnectionProvider);
                playerStorage = new H2PlayerStorage((H2ConnectionProvider) sqlConnectionProvider);
                break;
            }
            case MYSQL:
            {
                sqlConnectionProvider = new MySQLConnectionProvider(prepareDatabaseProperties(storageConfig));
                factionStorage = new MySQLFactionStorage(EagleFactionsPlugin.getPlugin().getLogger(), (MySQLConnectionProvider)sqlConnectionProvider);
                playerStorage = new MySQLPlayerStorage((MySQLConnectionProvider)sqlConnectionProvider);
                break;
            }
            case MARIADB:
            {
                sqlConnectionProvider = new MariaDbConnectionProvider(prepareDatabaseProperties(storageConfig));
                factionStorage = new MariaDbFactionStorage(EagleFactionsPlugin.getPlugin().getLogger(), (MariaDbConnectionProvider)sqlConnectionProvider);
                playerStorage = new MariaDbPlayerStorage((MariaDbConnectionProvider)sqlConnectionProvider);
                break;
            }
            case SQLITE:
            {
                DatabaseProperties databaseProperties = prepareDatabaseProperties(storageConfig);
                databaseProperties.setDatabaseFileDirectory(plugin.getConfigDir().resolve("data/sqlite"));
                sqlConnectionProvider = new SqliteConnectionProvider(databaseProperties);
                factionStorage = new SqliteFactionStorage(EagleFactionsPlugin.getPlugin().getLogger(), (SqliteConnectionProvider) sqlConnectionProvider);
                playerStorage = new SqlitePlayerStorage((SqliteConnectionProvider)sqlConnectionProvider);
                break;
            }
            case HOCON:
            default:
            {
                factionStorage = new HOCONFactionStorage(configDir);
                playerStorage = new HOCONPlayerStorage(configDir);
                break;
            }
        }

        if (storageType.isSql())
        {
            try
            {
                DatabaseInitializer.initialize(plugin, sqlConnectionProvider);
            }
            catch (SQLException | IOException e)
            {
                throw new IllegalStateException(format("Could not initialize the database for storage type = %s", storageType.getName()), e);
            }
        }

        plugin.printInfo(storageType.getName().toUpperCase() + " has been initialized!");

        this.backupStorage = new BackupStorage(factionStorage, playerStorage, configDir);
        this.storageTaskFactory = new StorageTaskFactory(playerStorage, factionStorage);
    }

    private void queueStorageTask(IStorageTask task)
    {
        this.executorService.submit(task);
    }

    @Override
    public void saveFaction(final Faction faction)
    {
        queueStorageTask(storageTaskFactory.saveFaction(faction));
        FactionsCache.saveFaction(faction);
    }

    @Override
    public boolean deleteFaction(final String factionName)
    {
        queueStorageTask(storageTaskFactory.deleteFaction(factionName));
        FactionsCache.removeFaction(factionName);
        return true;
    }

    @Override
    public @Nullable Faction getFaction(final String factionName)
    {
        try
        {
            Faction factionCache = FactionsCache.getFaction(factionName);
            if(factionCache != null)
                return factionCache;

            Faction faction = this.factionStorage.getFaction(factionName);
            if (faction == null)
                return null;

            FactionsCache.saveFaction(faction);

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
        final Set<Faction> factionSet = this.factionStorage.getFactions();
        for (final Faction faction : factionSet)
        {
            FactionsCache.saveFaction(faction);
        }
    }

    private void preparePlayerCache()
    {
        final Collection<FactionPlayer> players = this.playerStorage.getServerPlayers();
        final Collection<Faction> factions = FactionsCache.getFactionsMap().values();
        for (final FactionPlayer player : players)
        {
            FactionPlayer playerToSave = player;

            boolean factionExist = false;
            for (final Faction faction : factions)
            {
                if (faction.containsPlayer(player.getUniqueId()))
                {
                    factionExist = true;
                    playerToSave = new FactionPlayerImpl(player.getName(), player.getUniqueId(), faction.getName(), player.getPower(), player.getMaxPower(), player.diedInWarZone());
                    break;
                }
            }

            // Just in case someone deleted faction file :D
            if (!factionExist && player.getFactionName().isPresent())
            {
                playerToSave = new FactionPlayerImpl(player.getName(), player.getUniqueId(), null, player.getPower(), player.getMaxPower(), player.diedInWarZone());
            }
            FactionsCache.savePlayer(playerToSave);
        }
    }

    @Override
    public void reloadStorage()
    {
        FactionsCache.clear();
        this.factionStorage.load();

        reloadCache();
    }

    @Override
    public boolean savePlayer(final FactionPlayer factionPlayer)
    {
        queueStorageTask(storageTaskFactory.savePlayer(factionPlayer));
        FactionsCache.savePlayer(factionPlayer);
        return true;
    }

    @Override
    @Nullable
    public FactionPlayer getPlayer(final UUID playerUUID)
    {
        try
        {
            FactionPlayer cachedPlayer = FactionsCache.getPlayer(playerUUID);
            if(cachedPlayer != null)
                return cachedPlayer;

            FactionPlayer player = this.playerStorage.getPlayer(playerUUID);
            if (player == null)
                return null;

            FactionsCache.savePlayer(player);

            return player;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        //If it was not possible to get a faction then return null.
        return null;
    }

    @Override
    public Set<FactionPlayer> getServerPlayers()
    {
        return this.playerStorage.getServerPlayers();
    }

    @Override
    public Path createBackup()
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

    public void reloadCache()
    {
        EagleFactionsPlugin.getPlugin().getLogger().info("Reloading cache...");
        prepareFactionsCache();

        //Must be run after factions cache
        preparePlayerCache(); //Consider using cache that removes objects which have not been used for a long time.
    }

    private DatabaseProperties prepareDatabaseProperties(StorageConfig storageConfig)
    {
        DatabaseProperties databaseProperties = new DatabaseProperties();
        databaseProperties.setUsername(storageConfig.getStorageUsername());
        databaseProperties.setPassword(storageConfig.getStoragePassword());
        databaseProperties.setDatabaseName(storageConfig.getDatabaseName());
        databaseProperties.setDatabaseUrl(storageConfig.getDatabaseUrl());
        return databaseProperties;
    }
}
