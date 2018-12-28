package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.storage.h2.H2FactionStorage;
import io.github.aquerr.eaglefactions.storage.hocon.HOCONFactionStorage;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StorageManager
{
    //TODO: Consider using FactionsCache in this class only.

    private static StorageManager INSTANCE = null;

    private final IFactionStorage factionsStorage;
    private final Queue<IStorageTask> storageTaskQueue = new ConcurrentLinkedQueue<>();

    private final Thread storageThread;

    public static StorageManager getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new StorageManager(eagleFactions);
        else return INSTANCE;
    }

    private StorageManager(EagleFactions eagleFactions)
    {
        ConfigFields configFields = eagleFactions.getConfiguration().getConfigFields();
        Path configDir = eagleFactions.getConfigDir();
        switch(configFields.getStorageType().toLowerCase())
        {
            case "hocon":
                factionsStorage = new HOCONFactionStorage(configDir);
                break;
            case "h2":
                factionsStorage = new H2FactionStorage(eagleFactions);
                break;
                //TODO: Add SQLLite, JSON, etc...
//            case "sqllite":
//
//                break;
            default:
                factionsStorage = new HOCONFactionStorage(configDir);
                break;
        }
        prepareFactionsCache();

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

    public void deleteFaction(String factionName)
    {
        FactionsCache.removeFactionCache(factionName);
        queueStorageTask(new DeleteFactionTask(factionName));
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
}
