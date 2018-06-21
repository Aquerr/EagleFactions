package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagTypes;
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
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;


import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogic
{
    private static IStorage factionsStorage;

    public FactionLogic(Path configDir)
    {
        factionsStorage = new HOCONFactionStorage(configDir);
    }

    public static void reload()
    {
        factionsStorage.load();
    }

    public static Optional<Faction> getFactionByPlayerUUID(UUID playerUUID)
    {
        for (Faction faction : getFactions().values())
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
            else if(faction.Recruits.contains(playerUUID.toString()))
            {
                return Optional.of(faction);
            }
        }

        return Optional.empty();
    }

    public static Optional<Faction> getFactionByChunk(UUID worldUUID, Vector3i chunk)
    {
        for(Faction faction : getFactions().values())
        {
            if(faction.Claims.contains(worldUUID.toString() + "|" + chunk.toString()))
            {
                return Optional.of(faction);
            }
        }

        return Optional.empty();
    }

    public static @Nullable Faction getFactionByName(String factionName)
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
        Faction faction = getFactionByName(factionName);

        if (faction != null)
        {
            return faction.Leader;
        }

        return "";
    }

    public static List<String> getOfficers(String factionName)
    {
        Faction faction = getFactionByName(factionName);

        if (faction != null)
        {
            return faction.Officers;
        }

        return new ArrayList<>();
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

        for (String uuid : faction.Recruits)
        {
            if (!uuid.equals("") && PlayerManager.isPlayerOnline(UUID.fromString(uuid)))
            {
                factionPlayers.add(PlayerManager.getPlayer(UUID.fromString(uuid)).get());
            }
        }
        
        return factionPlayers;
    }
    
    public static Set<String> getFactionsNames()
    {
        return getFactions().keySet();
    }

//    public static @Nullable String getRealFactionName(String rawFactionName)
//    {
//        List<String> factionsNames = getFactionsNames();
//
//        return factionsNames.stream().filter(x->x.equalsIgnoreCase(rawFactionName)).findFirst().orElse(null);
//    }

    public static Map<String, Faction> getFactions()
    {
        return FactionsCache.getFactionsMap();
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
        Faction faction = getFactionByName(factionName);

        faction.Recruits.add(playerUUID.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void leaveFaction(UUID playerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        if (faction.Recruits.contains(playerUUID.toString()))
        {
            faction.Recruits.remove(playerUUID.toString());
        }
        else if(faction.Members.contains(playerUUID.toString()))
        {
            faction.Members.remove(playerUUID.toString());
        }
        else faction.Officers.remove(playerUUID.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void addAlly(String playerFactionName, String invitedFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction invitedFaction = getFactionByName(invitedFactionName);

        playerFaction.Alliances.add(invitedFactionName);
        invitedFaction.Alliances.add(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(invitedFaction);
    }

    public static List<String> getAlliances(String factionName)
    {
        Faction faction = getFactionByName(factionName);

        return faction.Alliances;
    }

    public static void removeAlly(String playerFactionName, String removedFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction removedFaction = getFactionByName(removedFactionName);

        playerFaction.Alliances.remove(removedFactionName);
        removedFaction.Alliances.remove(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(removedFaction);
    }

    public static void addEnemy(String playerFactionName, String enemyFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction enemyFaction = getFactionByName(enemyFactionName);

        playerFaction.Enemies.add(enemyFactionName);
        enemyFaction.Enemies.add(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(enemyFaction);
    }

    public static void removeEnemy(String playerFactionName, String enemyFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction enemyFaction = getFactionByName(enemyFactionName);

        playerFaction.Enemies.remove(enemyFactionName);
        enemyFaction.Enemies.remove(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(enemyFaction);
    }

    public static void addOfficerAndRemoveMember(String newOfficerUUIDAsString, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.Officers.add(newOfficerUUIDAsString);
        faction.Members.remove(newOfficerUUIDAsString);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void removeOfficerAndSetAsMember(String officerNameAsString, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.Officers.remove(officerNameAsString);
        faction.Members.add(officerNameAsString);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void setLeader(UUID newLeaderUUID, String playerFactionName)
    {
        Faction faction = getFactionByName(playerFactionName);

        if (!faction.Leader.equals(""))
        {
            faction.Officers.add(faction.Leader);
        }

        if (faction.Officers.contains(newLeaderUUID.toString()))
        {
            faction.Officers.remove(newLeaderUUID.toString());
            faction.Leader = newLeaderUUID.toString();
        }
        else if(faction.Members.contains(newLeaderUUID.toString()))
        {
            faction.Members.remove(newLeaderUUID.toString());
            faction.Leader = newLeaderUUID.toString();
        }
        else if(faction.Recruits.contains(newLeaderUUID.toString()))
        {
            faction.Recruits.remove(newLeaderUUID.toString());
            faction.Leader = newLeaderUUID.toString();
        }

        factionsStorage.addOrUpdateFaction(faction);
    }

//    public static List<String> getClaims(String factionName)
//    {
//        Faction faction = getFactionByName(factionName);
//
//        return faction.Claims;
//    }

    public static List<String> getAllClaims()
    {
        List<String> claimsList = new ArrayList<>();

        for (Faction faction : getFactions().values())
        {
            claimsList.addAll(faction.Claims);
        }

        return claimsList;
    }

    public static void addClaim(Faction faction, UUID worldUUID, Vector3i claimedChunk)
    {
        faction.Claims.add(worldUUID.toString() + "|" + claimedChunk.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void removeClaim(Faction faction, UUID worldUUID, Vector3i claimedChunk)
    {
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

    public static List<String> getFactionsTags()
    {
        List<Faction> factionsList = new ArrayList<>(getFactions().values());
        List<String> factionsTags = new ArrayList<>();

        for (Faction faction: factionsList)
        {
            factionsTags.add(faction.Tag.toPlain());
        }

        return factionsTags;
    }

    public static boolean hasOnlinePlayers(Faction faction)
    {
        if(faction.Leader != null && !faction.Leader.equals(""))
        {
            if(PlayerManager.isPlayerOnline(UUID.fromString(faction.Leader))) return true;
        }

        for (String playerUUID : faction.Officers)
        {
            if(PlayerManager.isPlayerOnline(UUID.fromString(playerUUID))) return true;
        }

        for (String playerUUID : faction.Members)
        {
            if(PlayerManager.isPlayerOnline(UUID.fromString(playerUUID))) return true;
        }

        for (String playerUUID : faction.Recruits)
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
        Faction faction = getFactionByName(factionName);

        if (faction.Recruits.contains(playerUUID.toString()))
        {
            faction.Recruits.remove(playerUUID.toString());
        }
        else if(faction.Members.contains(playerUUID.toString()))
        {
            faction.Members.remove(playerUUID.toString());
        }
        else faction.Officers.remove(playerUUID.toString());

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
                            if (addClaimByItems(player, faction, worldUUID, chunk)) player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                            else player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
                        }
                        else
                        {
                            addClaim(faction, worldUUID, chunk);
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
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
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MOVED_FROM_THE_CHUNK));
                    task.cancel();
                }
            }
        };
    }

    public static void startClaiming(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        if (MainLogic.isDelayedClaimingToggled())
        {
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.CLAIMING_HAS_BEEN_STARTED + " " + PluginMessages.STAY_IN_THE_CHUNK_FOR + " ", TextColors.GOLD, MainLogic.getClaimingDelay() + " " + PluginMessages.SECONDS, TextColors.GREEN, " " + PluginMessages.TO_CLAIM_IT));

            Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

            taskBuilder.delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(addClaimWithDelay(player, faction, worldUUID, chunk)).submit(EagleFactions.getEagleFactions());
        }
        else
        {
            if (MainLogic.shouldClaimByItems())
            {
                if (addClaimByItems(player, faction, worldUUID, chunk)) player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                else player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
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

    public static void toggleFlag(Faction faction, FactionMemberType factionMemberType, FactionFlagTypes factionFlagTypes, Boolean toggled)
    {
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags = faction.Flags;

        flags.get(factionMemberType).replace(factionFlagTypes, !toggled);

        faction.Flags = flags;

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void changeTagColor(Faction faction, TextColor textColor)
    {
        Text text = Text.of(textColor, faction.Tag.toPlainSingle());
        faction.Tag = text;

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void addMemberAndRemoveRecruit(String newMemberUUIDAsString, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.Members.add(newMemberUUIDAsString);
        faction.Recruits.remove(newMemberUUIDAsString);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void addRecruitAndRemoveMember(String newRecruitUUIDAsString, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.Recruits.add(newRecruitUUIDAsString);
        faction.Members.remove(newRecruitUUIDAsString);

        factionsStorage.addOrUpdateFaction(faction);
    }
}
