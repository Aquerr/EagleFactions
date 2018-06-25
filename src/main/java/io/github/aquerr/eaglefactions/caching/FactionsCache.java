package io.github.aquerr.eaglefactions.caching;

import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.storage.IStorage;
import org.spongepowered.api.scheduler.Task;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class FactionsCache
{
    private List<Faction> factionsList = new LinkedList<>();
    private Map<String, Faction> factionNameMap = new HashMap<>();
    private Map<String, Faction> playerUUIDMap = new HashMap<>();
    private Map<UUID, Map<Vector3i, String>> claims = new HashMap<>();
    private LinkedList<Faction> saveQueue = new LinkedList<>();
    private LinkedList<String> deleteQueue = new LinkedList<>();
    private Provider<IStorage> factionsStorage;

    private Settings settings;



    @Inject
    public FactionsCache(Provider<IStorage> factionsStorage, Settings settings, EagleFactions eagleFactions)
    {
        this.settings = settings;
        this.factionsStorage = factionsStorage;
        if (settings.isPeriodicSaving())
        {
            Task.builder().execute(task -> doSave()).interval(settings.getSaveDelay(), TimeUnit.MINUTES).submit(eagleFactions);
        }
    }

    public List<Faction> getFactions()
    {
        return factionsList;
    }


    public void addOrSetClaim(UUID world, Vector3i chunk, String faction)
    {
        Optional<String> previousClaim = getClaimOwner(world, chunk);
        if (previousClaim.isPresent())
        {
            Faction previous = getFaction(previousClaim.get()).get();
            previous.Claims.remove(world.toString() + "|" + chunk.toString());
            saveFaction(previous);
        }
        if (!claims.containsKey(world))
        {
            claims.put(world, new HashMap<>());
        }
        claims.get(world).put(chunk, faction.toLowerCase());
        Faction newFaction = getFaction(faction).get();
        newFaction.Claims.add(world.toString() + "|" + chunk.toString());
        saveFaction(newFaction);
    }

    public Optional<String> removeClaim(UUID world, Vector3i chunk)
    {
        if (claims.containsKey(world))
        {
            return Optional.ofNullable(claims.get(world).remove(chunk));
        }
        return Optional.empty();
    }

    public Optional<String> getClaimOwner(UUID world, Vector3i chunk)
    {
        return Optional.ofNullable(claims.getOrDefault(world, new HashMap<>()).get(chunk));
    }

    public void addFaction(Faction faction)
    {
        if (!factionNameMap.containsKey(faction.Name))
        {
            factionsList.add(faction);
            factionNameMap.put(faction.Name.toLowerCase(), faction);
            for (String member : faction.Members)
            {
                playerUUIDMap.put(member, faction);
            }
            for (String officer : faction.Officers)
            {
                playerUUIDMap.put(officer, faction);
            }
            playerUUIDMap.put(faction.Leader, faction);
            for (String claim : faction.Claims)
            {
                //Same as faction home
                String splitter = "\\|";
                String worldUUIDString = claim.split(splitter)[0];
                String vectorsString = claim.split(splitter)[1];

                String vectors[] = vectorsString.replace("(", "").replace(")", "").replace(" ", "").split(",");

                int x = Integer.valueOf(vectors[0]);
                int y = Integer.valueOf(vectors[1]);
                int z = Integer.valueOf(vectors[2]);

                Vector3i chunk = Vector3i.from(x, y, z);

                UUID worldUUID = UUID.fromString(worldUUIDString);
                addOrSetClaim(worldUUID, chunk, faction.Name.toLowerCase());
            }
        }
        saveFaction(faction);
        deleteQueue.removeIf(x -> x.equals(faction.Name));
        if (!settings.isPeriodicSaving())
        {
            doSave();
        }
    }

    public void removePlayer(UUID uuid)
    {
        playerUUIDMap.remove(uuid.toString());
    }

    public void updatePlayer(String player, String newFaction)
    {
        playerUUIDMap.put(player, getFaction(newFaction).get());
    }

    public void removeFaction(String factionName)
    {
        final String faction = factionName.toLowerCase();
        Optional<Faction> optionalFaction = factionsList.stream().filter(x -> x.Name.equals(faction)).findFirst();
        if (optionalFaction.isPresent())
        {
            factionsList.remove(optionalFaction.get());
            factionNameMap.remove(faction);
            for (String player : optionalFaction.get().Members)
            {
                playerUUIDMap.remove(player);
            }
            for (String player : optionalFaction.get().Officers)
            {
                playerUUIDMap.remove(player);
            }
            playerUUIDMap.remove(optionalFaction.get().Leader);
            removeAllClaims(factionName);
            deleteQueue.add(optionalFaction.get().Name);
            saveQueue.removeIf(x -> x.Name.equals(faction));
            if (!settings.isPeriodicSaving())
            {
                doSave();
            }
        }
        saveQueue.removeIf(x -> x.Name.equals(factionName));
    }

    public void removeAllClaims(String faction)
    {
        claims.values().forEach(a -> a.values().removeIf(x -> x.equals(faction.toLowerCase())));
    }

    public Optional<Faction> getFaction(String factionName)
    {
        if (!factionNameMap.containsKey(factionName.toLowerCase()))
        {
            return Optional.empty();
        }
        return Optional.of(factionNameMap.get(factionName.toLowerCase()));
    }

    public Optional<Faction> getFactionByPlayer(String uuid)
    {
        if (!playerUUIDMap.containsKey(uuid))
        {
            return Optional.empty();
        }
        return Optional.of(playerUUIDMap.get(uuid));
    }

    public Optional<Faction> getFactionByPlayer(UUID uuid)
    {
        return getFactionByPlayer(uuid.toString());
    }

    public Set<String> getFactionNames()
    {
        return factionNameMap.keySet();
    }

    public Optional<Faction> getFactionByChunk(UUID world, Vector3i chunk)
    {
        Optional<String> claim = getClaimOwner(world, chunk);
        if (claim.isPresent())
        {
            return getFaction(claim.get());
        }
        return Optional.empty();
    }

    public void doSave()
    {
        if (saveQueue.size() > 0)
        {
            if (settings.isPeriodicSaving())
            {
                EagleFactions.getPlugin().getLogger().info("Doing periodic save of factions data. (" + saveQueue.size() + " factions updated)");
            }
            while (saveQueue.size() > 0)
            {
                factionsStorage.get().addOrUpdateFaction(saveQueue.poll());
            }
            while (deleteQueue.size() > 0)
            {
                factionsStorage.get().removeFaction(deleteQueue.poll());
            }
            factionsStorage.get().saveChanges();
        }
    }

    public void saveFaction(Faction faction)
    {
        if (!saveQueue.contains(faction))
        {
            saveQueue.add(faction);
        }
        if (!settings.isPeriodicSaving())
        {
            doSave();
        }
    }

}
