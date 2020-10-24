package io.github.aquerr.eaglefactions.common.storage;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.StorageConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.common.storage.file.hocon.HOCONFactionStorage;
import io.github.aquerr.eaglefactions.common.storage.file.hocon.HOCONPlayerStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.h2.H2FactionStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.h2.H2PlayerStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.mariadb.MariaDbFactionStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.mariadb.MariaDbPlayerStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.mysql.MySQLFactionStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.mysql.MySQLPlayerStorage;
import io.github.aquerr.eaglefactions.common.storage.task.DeleteFactionTask;
import io.github.aquerr.eaglefactions.common.storage.task.IStorageTask;
import io.github.aquerr.eaglefactions.common.storage.task.SavePlayerTask;
import io.github.aquerr.eaglefactions.common.storage.task.UpdateFactionTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageManagerImpl implements StorageManager
{
    private final FactionStorage factionsStorage;
    private final PlayerStorage playerStorage;
    private final BackupStorage backupStorage;
    private final Path configDir;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); //Only one thread.

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
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Filling cache with data..."));
        prepareFactionsCache();
        preparePlayerCache(); //Consider using cache that removes objects which have not been used for a long time.
    }

    private void queueStorageTask(IStorageTask task)
    {
        this.executorService.submit(task);
    }

    @Override
    public void saveFaction(final Faction faction)
    {
        queueStorageTask(new UpdateFactionTask(faction, () -> this.factionsStorage.saveFaction(faction)));
        FactionsCache.saveFaction(faction);
    }

    @Override
    public boolean deleteFaction(final String factionName)
    {
        queueStorageTask(new DeleteFactionTask(factionName, () -> this.factionsStorage.deleteFaction(factionName)));
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

            Faction faction = this.factionsStorage.getFaction(factionName);
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
        final Set<Faction> factionSet = this.factionsStorage.getFactions();
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

            //Only for backwards compatibility
            if (!player.getFactionName().isPresent())
            {
                for (final Faction faction : factions)
                {
                    if (faction.containsPlayer(player.getUniqueId()))
                    {
                        playerToSave = new FactionPlayerImpl(player.getName(), player.getUniqueId(), faction.getName(), player.getPower(), player.getMaxPower(), player.diedInWarZone());
                    }
                }
                //Try to get correct faction for the player...
            }
            FactionsCache.savePlayer(playerToSave);
        }
    }

    @Override
    public void reloadStorage()
    {
        FactionsCache.clear();
        this.factionsStorage.load();
        prepareFactionsCache();

        //Must be run after factions.
        preparePlayerCache();
    }

//    @Override
//    public boolean checkIfPlayerExists(final UUID playerUUID, final String playerName)
//    {
//        return FactionsCache.getPlayer(playerUUID) != null;
//    }

    @Override
    public boolean savePlayer(final FactionPlayer factionPlayer)
    {
        queueStorageTask(new SavePlayerTask(factionPlayer, () -> this.playerStorage.savePlayer(factionPlayer)));
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
