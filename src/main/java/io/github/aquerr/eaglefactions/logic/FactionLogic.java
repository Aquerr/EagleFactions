package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.config.ConfigAccess;
import io.github.aquerr.eaglefactions.config.IConfig;
import io.github.aquerr.eaglefactions.config.FactionsConfig;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.services.PlayerService;
import io.github.aquerr.eaglefactions.services.PowerService;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;


import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogic
{
    private static IConfig factionsConfig = FactionsConfig.getConfig();

    public static String getFactionName(UUID playerUUID)
    {
        for (String factionName : FactionLogic.getFactionsNames())
        {
            if(FactionLogic.getMembers(factionName).contains(playerUUID.toString ()))
            {
                return factionName;
            }
            else if(FactionLogic.getLeader(factionName).equals(playerUUID.toString ()))
            {
                return factionName;
            }
            else if(FactionLogic.getOfficers(factionName).contains(playerUUID.toString ()))
            {
                return factionName;
            }
        }
        return null;
    }

    public static String getFactionNameByChunk(UUID worldUUID ,Vector3i chunk)
    {
        for(String factionName: getFactionsNames())
        {
            if(getClaims(factionName).contains(worldUUID.toString() + "|" + chunk.toString()))
            {
                return factionName;
            }
        }

        return "";
    }

    public static Faction getFaction(String factionName)
    {
        ConfigurationNode leaderNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "leader");

        String leaderUUID = "";
        if(leaderNode.getValue() != null) leaderUUID = leaderNode.getString();

        ConfigurationNode tagNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "tag");

        String factionTag = "";
        if(tagNode.getValue() != null) factionTag = tagNode.getString();

        Faction faction = new Faction(factionName, factionTag, leaderUUID);

        faction.Members = getMembers(factionName);
        faction.Officers = getOfficers(factionName);
        faction.Enemies = getEnemies(factionName);
        faction.Alliances = getAlliances(factionName);
        faction.Claims = getClaims(factionName);
        faction.Power = PowerService.getFactionPower(faction);

        return faction;
    }

    public static String getLeader(String factionName)
    {
        ConfigurationNode valueNode = ConfigAccess.getConfig(factionsConfig).getNode((Object[]) ("factions." + factionName + ".leader").split("\\."));

        if (valueNode.getValue() != null)
            return valueNode.getString();
        else
            return "";
    }

    public static List<String> getOfficers(String factionName)
    {
        ConfigurationNode officersNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName,"officers");

        if (officersNode.getValue() != null)
        {
            List<String> officersList = officersNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(officersList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static List<String> getMembers(String factionName)
    {
        ConfigurationNode membersNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName,"members");

        if (membersNode.getValue() != null)
        {
            List<String> membersList = membersNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(membersList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    
    public static List<UUID> getPlayers(String factionName)
    {
    	List<UUID> factionPlayers = new ArrayList<>();

    	factionPlayers.add(UUID.fromString(FactionLogic.getLeader(factionName)));
        
        for (String uuid : FactionLogic.getOfficers(factionName))
        {
        	factionPlayers.add(UUID.fromString(uuid));
        }
        
        for (String uuid : FactionLogic.getMembers(factionName))
        {
        	factionPlayers.add(UUID.fromString(uuid));
        }
        
        return factionPlayers;
    }
    
    public static List<Player> getPlayersOnline(String factionName)
    {
    	List<Player> factionPlayers = new ArrayList<>();
    	
    	String factionLeader = FactionLogic.getLeader(factionName);
    	if (PlayerService.isPlayerOnline(UUID.fromString(factionLeader)))
    	{
    		factionPlayers.add(PlayerService.getPlayer(UUID.fromString(factionLeader)).get());
    	}
        
        for (String uuid : FactionLogic.getOfficers(factionName))
        {
        	if (PlayerService.isPlayerOnline(UUID.fromString(uuid)))
        	{
        		factionPlayers.add(PlayerService.getPlayer(UUID.fromString(uuid)).get());
        	}
        }
        
        for (String uuid : FactionLogic.getMembers(factionName))
        {
        	if (PlayerService.isPlayerOnline(UUID.fromString(uuid)))
        	{
        		factionPlayers.add(PlayerService.getPlayer(UUID.fromString(uuid)).get());
        	}
        }
        
        return factionPlayers;
    }
    
    public static List<String> getFactionsNames()
    {
        if(ConfigAccess.getConfig(factionsConfig).getNode("factions").getValue() != null)
        {
            Set<Object> objectList =  ConfigAccess.getConfig(factionsConfig).getNode("factions").getChildrenMap().keySet();
            List<String> namesList = new ArrayList<>();

            for(Object object: objectList)
            {
                String factionName = String.valueOf(object);

                namesList.add(factionName);
            }

            return namesList;

        }

            return new ArrayList<>();
    }

    public static String getRealFactionName(String rawFactionName)
    {
        List<String> factionsNames = getFactionsNames();

        return factionsNames.stream().filter(x->x.equalsIgnoreCase(rawFactionName)).findFirst().orElse(null);
    }

    public static List<Faction> getFactions()
    {
        List<Faction> factionsList = new ArrayList<>();

        for (String factionName: getFactionsNames())
        {
            factionsList.add(getFaction(factionName));
        }

        return factionsList;
    }

    public static void createFaction(String factionName,String factionTag, UUID playerUUID)
    {
        try
        {
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "tag"}, factionTag.toString());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "leader"},(playerUUID.toString()));
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"},new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "home"},null);
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "members"},new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "enemies"},new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "alliances"}, new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "friendlyfire"}, false);
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "claims"}, new ArrayList<String>());
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static void disbandFaction(String factionName)
    {
        ConfigAccess.removeChild(factionsConfig, new Object[]{"factions"},factionName);
    }

    public static void joinFaction(UUID playerUUID, String factionName)
    {
        List<String> memberList = new ArrayList<>(getMembers(factionName));
        memberList.add(playerUUID.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, memberList);

    }

    public static void leaveFaction(UUID playerUUID, String factionName)
    {
        if(getMembers(factionName).contains(playerUUID.toString()))
        {
            List<String> memberList = new ArrayList<>(getMembers(factionName));

            memberList.remove(playerUUID.toString());

            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, memberList);

        }
        else if(getOfficers(factionName).contains(playerUUID.toString()))
        {
            List<String> officersList = new ArrayList<>(getOfficers(factionName));

            officersList.remove(playerUUID.toString());

            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"}, officersList);
        }
    }

    public static void addAlly(String playerFactionName, String invitedFactionName)
    {
        List<String> playerFactionAllianceList = new ArrayList<>(getAlliances(playerFactionName));
        List<String> invitedFactionAllianceList = new ArrayList<>(getAlliances(invitedFactionName));

        playerFactionAllianceList.add(invitedFactionName);
        invitedFactionAllianceList.add(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "alliances"}, playerFactionAllianceList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", invitedFactionName, "alliances"}, invitedFactionAllianceList);
    }

    public static List<String> getAlliances(String factionName)
    {
        ConfigurationNode allianceNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "alliances");

        if (allianceNode.getValue() != null)
        {
            List<String> alliancesList = allianceNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(alliancesList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static void removeAlly(String playerFactionName, String removedFaction)
    {
        List<String> playerFactionAllianceList = new ArrayList<>(getAlliances(playerFactionName));
        List<String> removedFactionAllianceList = new ArrayList<>(getAlliances(removedFaction));

        playerFactionAllianceList.remove(removedFaction);
        removedFactionAllianceList.remove(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "alliances"}, playerFactionAllianceList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", removedFaction, "alliances"}, removedFactionAllianceList);
    }

    public static List<String> getEnemies(String factionName)
    {

        ConfigurationNode enemiesNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "enemies");

        if (enemiesNode.getValue() != null)
        {
            List<String> enemiesList = enemiesNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(enemiesList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static void addEnemy(String playerFactionName, String enemyFactionName)
    {
        List<String> playerFactionEnemiesList = new ArrayList<>(getEnemies(playerFactionName));
        List<String> enemyFactionEnemiesList = new ArrayList<>(getEnemies(enemyFactionName));

        playerFactionEnemiesList.add(enemyFactionName);
        enemyFactionEnemiesList.add(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "enemies"}, playerFactionEnemiesList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", enemyFactionName, "enemies"}, enemyFactionEnemiesList);
    }

    public static void removeEnemy(String playerFactionName, String enemyFactionName)
    {
        List<String> playerFactionEnemiesList = new ArrayList<>(getEnemies(playerFactionName));
        List<String> enemyFactionEnemiesList = new ArrayList<>(getEnemies(enemyFactionName));

        playerFactionEnemiesList.remove(enemyFactionName);
        enemyFactionEnemiesList.remove(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "enemies"}, playerFactionEnemiesList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", enemyFactionName, "enemies"}, enemyFactionEnemiesList);
    }

    public static void addOfficerAndRemoveMember(String newOfficerUUIDAsString, String factionName)
    {
        addOfficer(UUID.fromString(newOfficerUUIDAsString), factionName);
        removeMember(UUID.fromString(newOfficerUUIDAsString), factionName);
    }

    public static void removeOfficerAndSetAsMember(String officerNameAsString, String factionName)
    {
        removeOfficer(UUID.fromString(officerNameAsString), factionName);
        addMember(UUID.fromString(officerNameAsString), factionName);
    }

    public static void setLeader(UUID newLeaderUUID, String playerFactionName)
    {
        if (!getLeader(playerFactionName).equals(""))
        {
            String lastLeader = getLeader(playerFactionName);
            addOfficerAndRemoveMember(lastLeader, playerFactionName);
        }

        if(getOfficers(playerFactionName).contains(newLeaderUUID.toString()))
        {
            removeOfficer(newLeaderUUID, playerFactionName);
        }
        else if(getMembers(playerFactionName).contains(newLeaderUUID.toString()))
        {
            removeMember(newLeaderUUID, playerFactionName);
        }

        ConfigAccess.setValueAndSave(factionsConfig, new Object[] {"factions", playerFactionName, "leader"}, newLeaderUUID.toString());
    }

    public static void addOfficer(UUID playerUUID, String factionName)
    {
        List<String> officersList = new ArrayList<>(getOfficers(factionName));
        officersList.add(playerUUID.toString());
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"}, officersList);
    }

    public static void addMember(UUID playerUUID, String factionName)
    {
        List<String> membersList = new ArrayList<>(getMembers(factionName));

        membersList.add(playerUUID.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, membersList);
    }

    public static void removeOfficer(UUID playerUUID, String factionName)
    {
        List<String> officersList = new ArrayList<>(getOfficers(factionName));
        officersList.remove(playerUUID.toString());
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"}, officersList);
    }

    public static void removeMember(UUID playerUUID, String factionName)
    {
        List<String> membersList = new ArrayList<>(getMembers(factionName));

        membersList.remove(playerUUID.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, membersList);
    }

    public static boolean getFactionFriendlyFire(String factionName)
    {
        ConfigurationNode friendlyFireNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "friendlyfire");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static void setFactionFriendlyFire(String factionName, boolean turnOn)
    {
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "friendlyfire"}, turnOn);
    }

    public static List<String> getClaims(String factionName)
    {
        ConfigurationNode claimsNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "claims");

        List<String> calimsList = claimsNode.getList(objectToStringTransformer);

        return calimsList;
    }

    public static void addClaim(String factionName, UUID worldUUID, Vector3i claimedChunk)
    {
        List<String> claimsList = new ArrayList<>(getClaims(factionName));

        claimsList.add(worldUUID.toString() + "|" + claimedChunk.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "claims"}, claimsList);
    }

    public static void removeClaim(String factionName, UUID worldUUID, Vector3i claimedChunk)
    {
        List<String> claimsList = new ArrayList<>(getClaims(factionName));

        claimsList.remove(worldUUID.toString() + "|" + claimedChunk.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "claims"}, claimsList);
    }

    public static boolean isClaimed(UUID worldUUID, Vector3i chunk)
    {
        for (String factionName: getFactionsNames())
        {
            List<String> factionClaims = getClaims(factionName);

            if(!factionClaims.isEmpty() && factionClaims != null)
            {
                for (String claim: factionClaims)
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



   // private static Function<Object,Chunk> objectToChunkTransformer = input ->
   // {
   //     if (input instanceof Chunk)
   //     {
   //         return (Chunk) input;
   //     }
   //     else
   //     {
   //         return null;
   //     }
   // };

    private static Function<Object,String> objectToStringTransformer = input ->
    {
        if (input instanceof String)
        {
            return (String) input;
        }
        else
        {
            return null;
        }
    };


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

        if(home != null)
        {
            String newHome = worldUUID.toString() + "|" + home.toString();
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions",factionName, "home"}, newHome);
        }
        else ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "home"}, null);
    }

    public static @Nullable FactionHome getHome(String factionName)
    {
        ConfigurationNode homeNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "home");

        if(homeNode.getValue() != null)
        {
            String homeString = homeNode.getString();
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
        ConfigurationNode tagNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "tag");

        return tagNode.getString();
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

//    public static boolean isHomeInWorld(UUID worldUUID, String factionName)
//    {
//        ConfigurationNode homeNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "home");
//
//        if(homeNode.getValue() != null)
//        {
//            if(homeNode.getString().contains(worldUUID.toString())) return true;
//            else return false;
//        }
//        return false;
//    }

    public static void removeClaims(String factionName)
    {
        List<String> claimsList = new ArrayList<>();

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "claims"}, claimsList);
    }

    public static void kickPlayer(UUID playerUUID, String factionName)
    {
        if(getMembers(factionName).contains(playerUUID.toString()))
        {
            List<String> memberList = new ArrayList<>(getMembers(factionName));

            memberList.remove(playerUUID.toString());

            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, memberList);

        }
        else if(getOfficers(factionName).contains(playerUUID.toString()))
        {
            List<String> officersList = new ArrayList<>(getOfficers(factionName));

            officersList.remove(playerUUID.toString());

            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"}, officersList);
        }
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
        Inventory inventory = player.getInventory();
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

                    inventory.query(itemStack.getItem()).poll(itemStack.getQuantity());
                }
            }

            addClaim(playerFactionName, worldUUID, chunk);
            return true;
        }
        else return false;
    }
}
