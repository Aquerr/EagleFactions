package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import com.sun.javaws.exceptions.InvalidArgumentException;
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
            if (faction.getLeader().equals(playerUUID.toString()))
            {
                return Optional.of(faction);
            }
            else if(faction.getOfficers().contains(playerUUID.toString()))
            {
                return Optional.of(faction);
            }
            else if(faction.getMembers().contains(playerUUID.toString()))
            {
                return Optional.of(faction);
            }
            else if(faction.getRecruits().contains(playerUUID.toString()))
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
            if(faction.getClaims().contains(worldUUID.toString() + "|" + chunk.toString()))
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

    public static UUID getLeader(String factionName)
    {
        Faction faction = getFactionByName(factionName);

        if (faction != null)
        {
            return faction.getLeader();
        }

        return UUID.randomUUID();
    }

    public static Set<UUID> getOfficers(String factionName)
    {
        Faction faction = getFactionByName(factionName);

        if (faction != null)
        {
            return faction.getOfficers();
        }

        return new HashSet<>();
    }

    public static List<Player> getOnlinePlayers(Faction faction)
    {
    	List<Player> factionPlayers = new ArrayList<>();
    	
    	UUID factionLeader = faction.getLeader();
    	if (!faction.getLeader().equals("") && PlayerManager.isPlayerOnline(factionLeader))
    	{
    		factionPlayers.add(PlayerManager.getPlayer(factionLeader).get());
    	}
        
        for (UUID uuid : faction.getOfficers())
        {
        	if (!uuid.equals("") && PlayerManager.isPlayerOnline(uuid))
        	{
        		factionPlayers.add(PlayerManager.getPlayer(uuid).get());
        	}
        }
        
        for (UUID uuid : faction.getMembers())
        {
        	if (!uuid.equals("") && PlayerManager.isPlayerOnline(uuid))
        	{
        		factionPlayers.add(PlayerManager.getPlayer(uuid).get());
        	}
        }

        for (UUID uuid : faction.getRecruits())
        {
            if (!uuid.equals("") && PlayerManager.isPlayerOnline(uuid))
            {
                factionPlayers.add(PlayerManager.getPlayer(uuid).get());
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
        Faction faction = new Faction(factionName, factionTag, playerUUID);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static boolean disbandFaction(String factionName)
    {
        return factionsStorage.removeFaction(factionName);
    }

    public static void joinFaction(UUID playerUUID, String factionName)
    {
        if(playerUUID == null || factionName.equals(""))
            throw new IllegalArgumentException("playerUUID can't be null and/or factionName can't be empty.");

        Faction faction = getFactionByName(factionName);
        faction.addRecruit(playerUUID);
        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void leaveFaction(UUID playerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        if (faction.getRecruits().contains(playerUUID))
        {
            faction.removeRecruit(playerUUID);
        }
        else if(faction.getMembers().contains(playerUUID))
        {
            faction.removeMember(playerUUID);
        }
        else faction.removeOfficer(playerUUID);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void addAlly(String playerFactionName, String invitedFactionName)
    {
        if(playerFactionName == null || invitedFactionName == null || playerFactionName.equals("") || invitedFactionName.equals(""))
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");

        Faction playerFaction = getFactionByName(playerFactionName);
        Faction invitedFaction = getFactionByName(invitedFactionName);

        playerFaction.addAlliance(invitedFactionName);
        invitedFaction.addAlliance(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(invitedFaction);
    }

    public static Set<String> getAlliances(String factionName)
    {
        Faction faction = getFactionByName(factionName);

        return faction.getAlliances();
    }

    public static void removeAlly(String playerFactionName, String removedFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction removedFaction = getFactionByName(removedFactionName);

        playerFaction.removeAlliance(removedFactionName);
        removedFaction.removeAlliance(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(removedFaction);
    }

    public static void addEnemy(String playerFactionName, String enemyFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction enemyFaction = getFactionByName(enemyFactionName);

        playerFaction.addEnemy(enemyFactionName);
        enemyFaction.addEnemy(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(enemyFaction);
    }

    public static void removeEnemy(String playerFactionName, String enemyFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction enemyFaction = getFactionByName(enemyFactionName);

        playerFaction.removeEnemy(enemyFactionName);
        enemyFaction.removeEnemy(playerFactionName);

        factionsStorage.addOrUpdateFaction(playerFaction);
        factionsStorage.addOrUpdateFaction(enemyFaction);
    }

    public static void addOfficerAndRemoveMember(UUID newOfficerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.addOfficer(newOfficerUUID);
        faction.removeMember(newOfficerUUID);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void removeOfficerAndSetAsMember(UUID officerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.removeOfficer(officerUUID);
        faction.addMember(officerUUID);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void setLeader(UUID newLeaderUUID, String playerFactionName)
    {
        Faction faction = getFactionByName(playerFactionName);

        if (!faction.getLeader().equals(""))
        {
            faction.addOfficer(faction.getLeader());
        }

        if (faction.getOfficers().contains(newLeaderUUID))
        {
            faction.removeOfficer(newLeaderUUID);
            faction.setLeader(newLeaderUUID);
        }
        else if(faction.getMembers().contains(newLeaderUUID))
        {
            faction.removeMember(newLeaderUUID);
            faction.setLeader(newLeaderUUID);
        }
        else if(faction.getRecruits().contains(newLeaderUUID))
        {
            faction.removeRecruit(newLeaderUUID);
            faction.setLeader(newLeaderUUID);
        }

        factionsStorage.addOrUpdateFaction(faction);
    }

//    public static List<String> getAllClaims(String factionName)
//    {
//        Faction faction = getFactionByName(factionName);
//
//        return faction.Claims;
//    }

    public static Set<String> getAllClaims()
    {
        return FactionsCache.getAllClaims();
    }

    public static void addClaim(Faction faction, UUID worldUUID, Vector3i claimedChunk)
    {
        faction.addClaim(worldUUID.toString() + "|" + claimedChunk.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void removeClaim(Faction faction, UUID worldUUID, Vector3i claimedChunk)
    {
        faction.removeClaim(worldUUID.toString() + "|" + claimedChunk.toString());

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static boolean isClaimed(UUID worldUUID, Vector3i chunk)
    {
        for (String claim : getAllClaims())
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
        Set<String> claimsList = faction.getClaims();

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
            faction.setHome(new FactionHome(worldUUID, home));
        }
        else
        {
            faction.setHome(null);
        }

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static List<String> getFactionsTags()
    {
        List<Faction> factionsList = new ArrayList<>(getFactions().values());
        List<String> factionsTags = new ArrayList<>();

        for (Faction faction: factionsList)
        {
            factionsTags.add(faction.getTag().toPlain());
        }

        return factionsTags;
    }

    public static boolean hasOnlinePlayers(Faction faction)
    {
        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            if(PlayerManager.isPlayerOnline(faction.getLeader())) return true;
        }

        for (UUID playerUUID : faction.getOfficers())
        {
            if(PlayerManager.isPlayerOnline(playerUUID)) return true;
        }

        for (UUID playerUUID : faction.getMembers())
        {
            if(PlayerManager.isPlayerOnline(playerUUID)) return true;
        }

        for (UUID playerUUID : faction.getRecruits())
        {
            if(PlayerManager.isPlayerOnline(playerUUID)) return true;
        }

        return false;
    }

    public static void removeClaims(Faction faction)
    {
        faction.removeAllClaims();

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void kickPlayer(UUID playerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        if (faction.getRecruits().contains(playerUUID))
        {
            faction.removeRecruit(playerUUID);
        }
        else if(faction.getMembers().contains(playerUUID))
        {
            faction.removeMember(playerUUID);
        }
        else faction.removeOfficer(playerUUID);

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

            taskBuilder.delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(addClaimWithDelay(player, faction, worldUUID, chunk)).submit(EagleFactions.getPlugin());
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

    public static void toggleFlag(Faction faction, FactionMemberType factionMemberType, FactionFlagTypes factionFlagTypes, Boolean flagValue)
    {
        faction.setFlag(factionMemberType, factionFlagTypes, flagValue);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void changeTagColor(Faction faction, TextColor textColor)
    {
        Text text = Text.of(textColor, faction.getTag().toPlainSingle());
        faction.setTag(text);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void addMemberAndRemoveRecruit(UUID newMemberUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.addMember(newMemberUUID);
        faction.removeRecruit(newMemberUUID);

        factionsStorage.addOrUpdateFaction(faction);
    }

    public static void addRecruitAndRemoveMember(UUID newRecruitUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.addRecruit(newRecruitUUID);
        faction.removeMember(newRecruitUUID);

        factionsStorage.addOrUpdateFaction(faction);
    }
}
