package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.services.PlayerService;
import io.github.aquerr.eaglefactions.services.PowerService;
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

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogic
{
    private static IStorage factionsStorage;

    public static void setupFactionLogic(Path configDir)
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
        return null;
    }

    public static String getFactionNameByChunk(UUID worldUUID ,Vector3i chunk)
    {
        for(Faction faction: getFactions())
        {
            if(faction.Claims.contains(worldUUID.toString() + "|" + chunk.toString()))
            {
                return faction.Name;
            }
        }

        return "";
    }

    public static Faction getFaction(String factionName)
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
        else return new ArrayList<String>();
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
    
    public static List<Player> getPlayersOnline(String factionName)
    {
        Faction faction = getFaction(factionName);

    	List<Player> factionPlayers = new ArrayList<>();
    	
    	String factionLeader = faction.Leader;
    	if (!faction.Leader.equals("") && PlayerService.isPlayerOnline(UUID.fromString(factionLeader)))
    	{
    		factionPlayers.add(PlayerService.getPlayer(UUID.fromString(factionLeader)).get());
    	}
        
        for (String uuid : faction.Officers)
        {
        	if (!uuid.equals("") && PlayerService.isPlayerOnline(UUID.fromString(uuid)))
        	{
        		factionPlayers.add(PlayerService.getPlayer(UUID.fromString(uuid)).get());
        	}
        }
        
        for (String uuid : faction.Members)
        {
        	if (!uuid.equals("") && PlayerService.isPlayerOnline(UUID.fromString(uuid)))
        	{
        		factionPlayers.add(PlayerService.getPlayer(UUID.fromString(uuid)).get());
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

    public static String getRealFactionName(String rawFactionName)
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
        List<String> memberList = new ArrayList<>(getMembers(factionName));
        memberList.add(playerUUID.toString());

        Faction faction = getFaction(factionName);

        faction.Members = memberList;

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

    public static void addClaim(String factionName, UUID worldUUID, Vector3i claimedChunk)
    {
        Faction faction = getFaction(factionName);

        faction.Claims.add(worldUUID.toString() + "|" + claimedChunk.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void removeClaim(String factionName, UUID worldUUID, Vector3i claimedChunk)
    {
        Faction faction = getFaction(factionName);

        faction.Claims.remove(worldUUID.toString() + "|" + claimedChunk.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static boolean isClaimed(UUID worldUUID, Vector3i chunk)
    {
        for (Faction faction: getFactions())
        {
            if(!faction.Claims.isEmpty())
            {
                for (String claim: faction.Claims)
                {
                    if(claim.equalsIgnoreCase(worldUUID.toString() + "|" + chunk.toString()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isClaimConnected(String factionName, UUID worldUUID, Vector3i chunk)
    {
        List<String> claimsList = getClaims(factionName);

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

    public static void setHome(@Nullable UUID worldUUID ,String factionName, @Nullable Vector3i home)
    {
        //TODO: Add new property for home in faction class.
        Faction faction = getFaction(factionName);

        if(home != null)
        {
            String newHome = worldUUID.toString() + "|" + home.toString();

            faction.Home = newHome;
        }
        else
        {
            faction.Home = null;
        }

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static @Nullable FactionHome getHome(String factionName)
    {
        Faction faction = getFaction(factionName);

        if(faction.Home != null && !faction.Home.equals(""))
        {
            String homeString = faction.Home;
            String splitter = "\\|";

            String worldUUIDString = homeString.split(splitter)[0];
            String vectorsString = homeString.split(splitter)[1];

            String vectors[] = vectorsString.replace("(", "").replace(")", "").replace(" ", "").split(",");

             int x = Integer.valueOf(vectors[0]);
             int y = Integer.valueOf(vectors[1]);
             int z = Integer.valueOf(vectors[2]);

             Vector3i blockPosition = Vector3i.from(x, y, z);
             UUID worldUUID = UUID.fromString(worldUUIDString);

             return new FactionHome(worldUUID, blockPosition);
        }
        else
        {
            return null;
        }
    }

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
            if(PlayerService.isPlayerOnline(UUID.fromString(FactionLogic.getLeader(factionName)))) return true;
        }

        for (String playerUUID: getOfficers(factionName))
        {
            if(PlayerService.isPlayerOnline(UUID.fromString(playerUUID))) return true;
        }

        for (String playerUUID: getMembers(factionName))
        {
            if(PlayerService.isPlayerOnline(UUID.fromString(playerUUID))) return true;
        }

        return false;
    }

    public static void removeClaims(String factionName)
    {
        Faction faction = getFaction(factionName);

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

    public static void addClaimWithDelay(Player player, String playerFactionName, UUID worldUUID, Vector3i chunk, int seconds)
    {
        if (chunk.toString().equals(player.getLocation().getChunkPosition().toString()))
        {
            if (seconds >= MainLogic.getClaimingDelay())
            {
                if (MainLogic.shouldClaimByItems())
                {
                    if (addClaimByItems(player, playerFactionName, worldUUID, chunk)) player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                    else player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have enough resources to claim a territory!"));
                }
                else
                {
                    addClaim(playerFactionName, worldUUID, chunk);
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RESET, seconds));
                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
                taskBuilder.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        addClaimWithDelay(player, playerFactionName, worldUUID, chunk, seconds + 1);
                    }

                }).delay(1, TimeUnit.SECONDS).name("EagleFactions - Claim").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());
            }
        }
        else
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You moved from the chunk!"));
        }
    }

    public static void startClaiming(Player player, String playerFactionName, UUID worldUUID, Vector3i chunk)
    {
        if (MainLogic.isDelayedClaimingToggled())
        {
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Claiming has been started! Stay in the chunk for ", TextColors.GOLD, MainLogic.getClaimingDelay() + " seconds", TextColors.GREEN, " to claim it!"));
            addClaimWithDelay(player, playerFactionName, worldUUID, chunk, 0);
        }
        else
        {
            if (MainLogic.shouldClaimByItems())
            {
                if (addClaimByItems(player, playerFactionName, worldUUID, chunk)) player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                else player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have enough resources to claim a territory!"));
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                addClaim(playerFactionName, worldUUID, chunk);
            }
        }
    }

    private static boolean addClaimByItems(Player player, String playerFactionName, UUID worldUUID, Vector3i chunk)
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

            addClaim(playerFactionName, worldUUID, chunk);
            return true;
        }
        else return false;
    }
}
