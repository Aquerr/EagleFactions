package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagType;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.storage.HOCONFactionStorage;
import io.github.aquerr.eaglefactions.storage.IStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;


import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogic
{
    private static IStorage factionsStorage;

    public static void setup(Path configDir)
    {
        //TODO: Choose which storage should be used. (HOCON, MySQL etc.)
        factionsStorage = new HOCONFactionStorage(configDir);
    }

    public static void reload()
    {
        factionsStorage.load();
    }

    public static String getFactionName(UUID playerUUID)
    {
        for (Faction faction : FactionLogic.getFactions())
        {
            if(faction.Members.contains(playerUUID.toString()))
            {
                return faction.Name;
            }
            else if(faction.Leader.equals(playerUUID.toString()))
            {
                return faction.Name;
            }
            else if(faction.Officers.contains(playerUUID.toString()))
            {
                return faction.Name;
            }
        }
        return "";
    }

    public static Optional<Faction> getFactionByPlayerUUID(UUID playerUUID)
    {
        for (Faction faction : getFactions())
        {
            if (faction.Leader.equals(playerUUID.toString()))
            {
                return Optional.of(faction);
            }
            else if(faction.Officers.contains(playerUUID.toString()))
            {
                return Optional.of(faction);
            }
            else if(faction.Members.contains(playerUUID.toString()))
            {
                return Optional.of(faction);
            }
        }

        return Optional.empty();
    }

    public static Optional<Faction> getFactionByChunk(UUID worldUUID, Vector3i chunk)
    {
        for(Faction faction: getFactions())
        {
            if(faction.Claims.contains(worldUUID.toString() + "|" + chunk.toString()))
            {
                return Optional.of(faction);
            }
        }

        return Optional.empty();
    }

    public static @Nullable Faction getFaction(String factionName)
    {
        Faction faction = factionsStorage.getFaction(factionName);

        if (faction != null)
        {
            return faction;
        }

        return null;
    }

    public static String getLeader(String factionName)
    {
        Faction faction = getFaction(factionName);

        if (faction != null)
        {
            return faction.Leader;
        }

        return "";
    }

    public static List<String> getOfficers(String factionName)
    {
        Faction faction = getFaction(factionName);

        if (faction != null)
        {
            return faction.Officers;
        }

        return new ArrayList<>();
    }

    public static List<String> getMembers(String factionName)
    {
        Faction faction = getFaction(factionName);

        if (faction != null)
        {
            return faction.Members;
        }
        else return new ArrayList<>();
    }

    
    public static List<UUID> getPlayers(String factionName)
    {
        Faction faction = getFaction(factionName);

    	List<UUID> factionPlayers = new ArrayList<>();

    	factionPlayers.add(UUID.fromString(faction.Leader));
        
        for (String uuid : faction.Officers)
        {
        	factionPlayers.add(UUID.fromString(uuid));
        }
        
        for (String uuid : faction.Members)
        {
        	factionPlayers.add(UUID.fromString(uuid));
        }
        
        return factionPlayers;
    }
    
    public static List<Player> getOnlinePlayers(Faction faction)
    {
    	List<Player> factionPlayers = new ArrayList<>();
    	
    	String factionLeader = faction.Leader;
    	if (!faction.Leader.equals("") && PlayerManager.isPlayerOnline(UUID.fromString(factionLeader)))
    	{
    		factionPlayers.add(PlayerManager.getPlayer(UUID.fromString(factionLeader)).get());
    	}
        
        for (String uuid : faction.Officers)
        {
        	if (!uuid.equals("") && PlayerManager.isPlayerOnline(UUID.fromString(uuid)))
        	{
        		factionPlayers.add(PlayerManager.getPlayer(UUID.fromString(uuid)).get());
        	}
        }
        
        for (String uuid : faction.Members)
        {
        	if (!uuid.equals("") && PlayerManager.isPlayerOnline(UUID.fromString(uuid)))
        	{
        		factionPlayers.add(PlayerManager.getPlayer(UUID.fromString(uuid)).get());
        	}
        }
        
        return factionPlayers;
    }
    
    public static List<String> getFactionsNames()
    {
        List<Faction> factions = getFactions();
        List<String> namesList = new ArrayList<>();

        for(Faction faction: factions)
        {
            namesList.add(faction.Name);
        }

        return namesList;
    }

    public static @Nullable String getRealFactionName(String rawFactionName)
    {
        List<String> factionsNames = getFactionsNames();

        return factionsNames.stream().filter(x->x.equalsIgnoreCase(rawFactionName)).findFirst().orElse(null);
    }

    public static List<Faction> getFactions()
    {
        return factionsStorage.getFactions();
    }

    public static void createFaction(String factionName,String factionTag, UUID playerUUID)
    {
        Faction faction = new Faction(factionName, factionTag, playerUUID.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static boolean disbandFaction(String factionName)
    {
        return factionsStorage.removeFaction(factionName);
    }

    public static void joinFaction(UUID playerUUID, String factionName)
    {
        Faction faction = getFaction(factionName);

        faction.Members.add(playerUUID.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void leaveFaction(UUID playerUUID, String factionName)
    {
        Faction faction = getFaction(factionName);

        if(getMembers(factionName).contains(playerUUID.toString()))
        {
            faction.Members.remove(playerUUID.toString());
        }
        else if(getOfficers(factionName).contains(playerUUID.toString()))
        {
            faction.Officers.remove(playerUUID.toString());
        }

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void addAlly(String playerFactionName, String invitedFactionName)
    {
        Faction playerFaction = getFaction(playerFactionName);
        Faction invitedFaction = getFaction(invitedFactionName);

        playerFaction.Alliances.add(invitedFactionName);
        invitedFaction.Alliances.add(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(invitedFaction);
    }

    public static List<String> getAlliances(String factionName)
    {
        Faction faction = getFaction(factionName);

        return faction.Alliances;
    }

    public static void removeAlly(String playerFactionName, String removedFactionName)
    {
        Faction playerFaction = getFaction(playerFactionName);
        Faction removedFaction = getFaction(removedFactionName);

        playerFaction.Alliances.remove(removedFactionName);
        removedFaction.Alliances.remove(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(removedFaction);
    }

    public static List<String> getEnemies(String factionName)
    {
        Faction faction = getFaction(factionName);

        return faction.Enemies;
    }

    public static void addEnemy(String playerFactionName, String enemyFactionName)
    {
        Faction playerFaction = getFaction(playerFactionName);
        Faction enemyFaction = getFaction(enemyFactionName);

        playerFaction.Enemies.add(enemyFactionName);
        enemyFaction.Enemies.add(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(enemyFaction);
    }

    public static void removeEnemy(String playerFactionName, String enemyFactionName)
    {
        Faction playerFaction = getFaction(playerFactionName);
        Faction enemyFaction = getFaction(enemyFactionName);

        playerFaction.Enemies.remove(enemyFactionName);
        enemyFaction.Enemies.remove(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(enemyFaction);
    }

    public static void addOfficerAndRemoveMember(String newOfficerUUIDAsString, String factionName)
    {
        Faction faction = getFaction(factionName);

        faction.Officers.add(newOfficerUUIDAsString);
        faction.Members.remove(newOfficerUUIDAsString);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void removeOfficerAndSetAsMember(String officerNameAsString, String factionName)
    {
        Faction faction = getFaction(factionName);

        faction.Officers.remove(officerNameAsString);
        faction.Members.add(officerNameAsString);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void setLeader(UUID newLeaderUUID, String playerFactionName)
    {
        Faction faction = getFaction(playerFactionName);

        if (!faction.Leader.equals(""))
        {
            faction.Officers.add(faction.Leader);
        }

        if (faction.Officers.contains(newLeaderUUID.toString()))
        {
            faction.Officers.remove(newLeaderUUID);
            faction.Leader = newLeaderUUID.toString();
        }
        else if(faction.Members.contains(newLeaderUUID.toString()))
        {
            faction.Members.remove(newLeaderUUID.toString());
            faction.Leader = newLeaderUUID.toString();
        }

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static List<String> getClaims(String factionName)
    {
        Faction faction = getFaction(factionName);

        return faction.Claims;
    }

    public static List<String> getAllClaims()
    {
        List<String> claimsList = new ArrayList<>();

        for (Faction faction : getFactions())
        {
            claimsList.addAll(faction.Claims);
        }

        return claimsList;

//        List<String> claimsList = getFactions().stream().map(x->x.Claims).flatMap(List::stream).collect(Collectors.toList());
//
//        return claimsList;
    }

    public static void addClaim(Faction faction, UUID worldUUID, Vector3i claimedChunk)
    {
        //Faction faction = getFaction(faction);

        faction.Claims.add(worldUUID.toString() + "|" + claimedChunk.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void removeClaim(Faction faction, UUID worldUUID, Vector3i claimedChunk)
    {
        //Faction faction = getFaction(factionName);

        faction.Claims.remove(worldUUID.toString() + "|" + claimedChunk.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static boolean isClaimed(UUID worldUUID, Vector3i chunk)
    {
        for (String claim: getAllClaims())
        {
            if(claim.equalsIgnoreCase(worldUUID.toString() + "|" + chunk.toString()))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isClaimConnected(Faction faction, UUID worldUUID, Vector3i chunk)
    {
        List<String> claimsList = faction.Claims;

        for (String object: claimsList)
        {
            if(object.contains(worldUUID.toString()))
            {
                String vectors[] = object.replace(worldUUID.toString() + "|", "").replace("(", "").replace(")", "").replace(" ", "").split(",");

                int x = Integer.valueOf(vectors[0]);
                int y = Integer.valueOf(vectors[1]);
                int z = Integer.valueOf(vectors[2]);

                Vector3i claim = Vector3i.from(x, y, z);

                if((claim.getX() == chunk.getX()) && ((claim.getZ() + 1 == chunk.getZ()) || (claim.getZ() - 1 == chunk.getZ())))
                {
                    return true;
                }
                else if((claim.getZ() == chunk.getZ()) && ((claim.getX() + 1 == chunk.getX()) || (claim.getX() - 1 == chunk.getX())))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setHome(@Nullable UUID worldUUID , Faction faction, @Nullable Vector3i home)
    {
        if(home != null && worldUUID != null)
        {
            faction.Home = new FactionHome(worldUUID, home);
        }
        else
        {
            faction.Home = null;
        }

        factionsStorage.addOrUpdateFaction(faction);
    }

//    public static @Nullable FactionHome getHome(Faction faction)
//    {
//
//    }

    public static List<String> getFactionsTags()
    {
        List<Faction> factionsList = getFactions();
        List<String> factionsTags = new ArrayList<>();

        for (Faction faction: factionsList)
        {
            factionsTags.add(faction.Tag);
        }

        return factionsTags;
    }

    public static String getFactionTag(String factionName)
    {
        Faction faction = getFaction(factionName);

        return faction.Tag;
    }

    public static boolean hasOnlinePlayers(String factionName)
    {
        if(FactionLogic.getLeader(factionName) != null && !FactionLogic.getLeader(factionName).equals(""))
        {
            if(PlayerManager.isPlayerOnline(UUID.fromString(FactionLogic.getLeader(factionName)))) return true;
        }

        for (String playerUUID: getOfficers(factionName))
        {
            if(PlayerManager.isPlayerOnline(UUID.fromString(playerUUID))) return true;
        }

        for (String playerUUID: getMembers(factionName))
        {
            if(PlayerManager.isPlayerOnline(UUID.fromString(playerUUID))) return true;
        }

        return false;
    }

    public static void removeClaims(Faction faction)
    {
        faction.Claims = new ArrayList<>();

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void kickPlayer(UUID playerUUID, String factionName)
    {
        Faction faction = getFaction(factionName);

        if(faction.Members.contains(playerUUID.toString()))
        {
            faction.Members.remove(playerUUID.toString());
        }
        else if(faction.Officers.contains(playerUUID.toString()))
        {
            faction.Officers.remove(playerUUID.toString());
        }

        factionsStorage.addOrUpdateFaction(faction);
    }

    private static Consumer<Task> addClaimWithDelay(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        return new Consumer<Task>()
        {
            int seconds = 1;

            @Override
            public void accept(Task task)
            {
                if (chunk.toString().equals(player.getLocation().getChunkPosition().toString()))
                {
                    if (seconds >= MainLogic.getClaimingDelay())
                    {
                        if (MainLogic.shouldClaimByItems())
                        {
                            if (addClaimByItems(player, faction, worldUUID, chunk)) player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                            else player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have enough resources to claim a territory!"));
                        }
                        else
                        {
                            addClaim(faction, worldUUID, chunk);
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RESET, seconds));
                        seconds++;
                    }
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You moved from the chunk!"));
                    task.cancel();
                }
            }
        };
    }

    public static void startClaiming(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        if (MainLogic.isDelayedClaimingToggled())
        {
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Claiming has been started! Stay in the chunk for ", TextColors.GOLD, MainLogic.getClaimingDelay() + " seconds", TextColors.GREEN, " to claim it!"));

            Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

            taskBuilder.delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(addClaimWithDelay(player, faction, worldUUID, chunk)).submit(EagleFactions.getEagleFactions());
        }
        else
        {
            if (MainLogic.shouldClaimByItems())
            {
                if (addClaimByItems(player, faction, worldUUID, chunk)) player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                else player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have enough resources to claim a territory!"));
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                addClaim(faction, worldUUID, chunk);
            }
        }
    }

    private static boolean addClaimByItems(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        HashMap<String, Integer> requiredItems = MainLogic.getRequiredItemsToClaim();
        PlayerInventory inventory = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(PlayerInventory.class));
        int allRequiredItems = requiredItems.size();
        int foundItems = 0;

        for (String requiredItem : requiredItems.keySet())
        {
            String[] idAndVariant = requiredItem.split(":");

            String itemId = idAndVariant[0] + ":" + idAndVariant[1];
            Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

            if(itemType.isPresent())
            {
                ItemStack itemStack = ItemStack.builder()
                        .itemType(itemType.get()).build();
                itemStack.setQuantity(requiredItems.get(requiredItem));

                if (idAndVariant.length == 3)
                {
                    if (itemType.get().getBlock().isPresent())
                    {
                        int variant = Integer.parseInt(idAndVariant[2]);
                        BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                        itemStack = ItemStack.builder().fromBlockState(blockState).build();
                    }
                }

                if (inventory.contains(itemStack))
                {
                    foundItems += 1;
                }
                else
                {
                    return false;
                }
            }
        }

        if (allRequiredItems == foundItems)
        {
            for (String requiredItem : requiredItems.keySet())
            {
                String[] idAndVariant = requiredItem.split(":");
                String itemId = idAndVariant[0] + ":" + idAndVariant[1];

                Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

                if(itemType.isPresent())
                {
                    ItemStack itemStack = ItemStack.builder()
                            .itemType(itemType.get()).build();
                    itemStack.setQuantity(requiredItems.get(requiredItem));

                    if (idAndVariant.length == 3)
                    {
                        if (itemType.get().getBlock().isPresent())
                        {
                            int variant = Integer.parseInt(idAndVariant[2]);
                            BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                            itemStack = ItemStack.builder().fromBlockState(blockState).build();
                        }
                    }

                    inventory.query(QueryOperationTypes.ITEM_TYPE.of(itemType.get())).poll(itemStack.getQuantity());
                }
            }

            addClaim(faction, worldUUID, chunk);
            return true;
        }
        else return false;
    }

    public static void toggleFlag(Faction faction, FactionMemberType factionMemberType, FactionFlagType factionFlagType, Boolean toggled)
    {
        Map<FactionMemberType, Map<FactionFlagType, Boolean>> flags = faction.Flags;

        flags.get(factionMemberType).replace(factionFlagType, !toggled);

        faction.Flags = flags;

        factionsStorage.addOrUpdateFaction(faction);
    }
}
