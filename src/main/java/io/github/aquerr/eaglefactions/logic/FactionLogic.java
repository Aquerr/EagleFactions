package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.entities.*;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import io.github.aquerr.eaglefactions.storage.StorageManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogic
{
    private static FactionLogic INSTANCE = null;

    private final StorageManager storageManager;
    private final ConfigFields _configFields;
    private final PlayerManager _playerManager;

    private final UUID DUMMY_UUID = new UUID(0, 0);

    public static FactionLogic getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new FactionLogic(eagleFactions);
        else return INSTANCE;
    }

    public FactionLogic(EagleFactions plugin)
    {
        INSTANCE = this;
        _configFields = plugin.getConfiguration().getConfigFields();
        _playerManager = plugin.getPlayerManager();
        this.storageManager = plugin.getStorageManager();
    }

    public Optional<Faction> getFactionByPlayerUUID(UUID playerUUID)
    {
        for(Faction faction : getFactions().values())
        {
            if(faction.getLeader() != null && faction.getLeader().equals(playerUUID))
            {
                return Optional.of(faction);
            }
            else if(faction.getOfficers().contains(playerUUID))
            {
                return Optional.of(faction);
            }
            else if(faction.getMembers().contains(playerUUID))
            {
                return Optional.of(faction);
            }
            else if(faction.getRecruits().contains(playerUUID))
            {
                return Optional.of(faction);
            }
        }

        return Optional.empty();
    }

    public Optional<Faction> getFactionByChunk(UUID worldUUID, Vector3i chunk)
    {
        Claim claim = new Claim(worldUUID, chunk);
        for(Faction faction : getFactions().values())
        {
            if(faction.getClaims().contains(claim))
            {
                return Optional.of(faction);
            }
        }

        return Optional.empty();
    }

    public @Nullable
    Faction getFactionByName(String factionName)
    {
        Faction faction = storageManager.getFaction(factionName);

        if(faction != null)
        {
            return faction;
        }

        return null;
    }

    public List<Player> getOnlinePlayers(Faction faction)
    {

        List<Player> factionPlayers = new ArrayList<>();

        UUID factionLeader = faction.getLeader();
        if(!faction.getLeader().equals(DUMMY_UUID) && _playerManager.isPlayerOnline(factionLeader))
        {
            factionPlayers.add(_playerManager.getPlayer(factionLeader).get());
        }

        for(UUID uuid : faction.getOfficers())
        {
            if(_playerManager.isPlayerOnline(uuid))
            {
                factionPlayers.add(_playerManager.getPlayer(uuid).get());
            }
        }

        for(UUID uuid : faction.getMembers())
        {
            if(_playerManager.isPlayerOnline(uuid))
            {
                factionPlayers.add(_playerManager.getPlayer(uuid).get());
            }
        }

        for(UUID uuid : faction.getRecruits())
        {
            if(_playerManager.isPlayerOnline(uuid))
            {
                factionPlayers.add(_playerManager.getPlayer(uuid).get());
            }
        }

        return factionPlayers;
    }

    public Set<String> getFactionsNames()
    {
        return getFactions().keySet();
    }

    public Map<String, Faction> getFactions()
    {
        return FactionsCache.getFactionsMap();
    }

    public void addFaction(Faction faction)
    {
        storageManager.addOrUpdateFaction(faction);
    }

    public boolean disbandFaction(String factionName)
    {
        return this.storageManager.deleteFaction(factionName);
    }

    public void joinFaction(UUID playerUUID, String factionName)
    {
        if(playerUUID == null || factionName.equals(""))
        {
            throw new IllegalArgumentException("playerUUID can't be null and/or factionName can't be empty.");
        }

        Faction faction = getFactionByName(factionName);
        faction.addRecruit(playerUUID);
        storageManager.addOrUpdateFaction(faction);
    }

    public void leaveFaction(UUID playerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        if(faction.getRecruits().contains(playerUUID))
        {
            faction.removeRecruit(playerUUID);
        }
        else if(faction.getMembers().contains(playerUUID))
        {
            faction.removeMember(playerUUID);
        }
        else
        {
            faction.removeOfficer(playerUUID);
        }

        storageManager.addOrUpdateFaction(faction);
    }

    public void addAlly(String playerFactionName, String invitedFactionName)
    {
        if(playerFactionName == null || invitedFactionName == null || playerFactionName.equals("") || invitedFactionName.equals(""))
        {
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");
        }

        Faction playerFaction = getFactionByName(playerFactionName);
        Faction invitedFaction = getFactionByName(invitedFactionName);

        playerFaction.addAlliance(invitedFactionName);
        invitedFaction.addAlliance(playerFactionName);

        storageManager.addOrUpdateFaction(playerFaction);
        storageManager.addOrUpdateFaction(invitedFaction);
    }

    public Set<String> getAlliances(String factionName)
    {
        Faction faction = getFactionByName(factionName);

        return faction.getAlliances();
    }

    public void removeAlly(String playerFactionName, String removedFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction removedFaction = getFactionByName(removedFactionName);

        playerFaction.removeAlliance(removedFactionName);
        removedFaction.removeAlliance(playerFactionName);

        storageManager.addOrUpdateFaction(playerFaction);
        storageManager.addOrUpdateFaction(removedFaction);
    }

    public void addEnemy(String playerFactionName, String enemyFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction enemyFaction = getFactionByName(enemyFactionName);

        playerFaction.addEnemy(enemyFactionName);
        enemyFaction.addEnemy(playerFactionName);

        storageManager.addOrUpdateFaction(playerFaction);
        storageManager.addOrUpdateFaction(enemyFaction);
    }

    public void removeEnemy(String playerFactionName, String enemyFactionName)
    {
        Faction playerFaction = getFactionByName(playerFactionName);
        Faction enemyFaction = getFactionByName(enemyFactionName);

        playerFaction.removeEnemy(enemyFactionName);
        enemyFaction.removeEnemy(playerFactionName);

        storageManager.addOrUpdateFaction(playerFaction);
        storageManager.addOrUpdateFaction(enemyFaction);
    }

    public void addOfficerAndRemoveMember(UUID newOfficerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.addOfficer(newOfficerUUID);
        faction.removeMember(newOfficerUUID);

        storageManager.addOrUpdateFaction(faction);
    }

    public void removeOfficerAndSetAsMember(UUID officerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.removeOfficer(officerUUID);
        faction.addMember(officerUUID);

        storageManager.addOrUpdateFaction(faction);
    }

    public void setLeader(UUID newLeaderUUID, String playerFactionName)
    {
        Faction faction = getFactionByName(playerFactionName);

        if(!faction.getLeader().equals(""))
        {
            faction.addOfficer(faction.getLeader());
        }

        if(faction.getOfficers().contains(newLeaderUUID))
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

        storageManager.addOrUpdateFaction(faction);
    }

//    public  List<String> getAllClaims(String factionName)
//    {
//        Faction faction = getFactionByName(factionName);
//
//        return faction.Claims;
//    }

    public Set<Claim> getAllClaims()
    {
        return FactionsCache.getAllClaims();
    }

    public void addClaims(Faction faction, List<Claim> claims)
    {
        for(final Claim claim : claims)
        {
            faction.addClaim(claim);
            World world = Sponge.getServer().getWorld(claim.getWorldUUID()).get();
            Vector3i chunkPosition = claim.getChunkPosition();
            double x = (chunkPosition.getX() << 4) + 8;
            double z = (chunkPosition.getZ() << 4) + 8;
            double y = world.getHighestYAt((int)x, (int)z);
            Vector3d position = new Vector3d(x, y, z);
            world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.CLOUD).quantity(400).offset(new Vector3d(4, 1, 4)).build(), position);
            world.playSound(SoundTypes.ITEM_ARMOR_EQUIP_IRON, position, 5, -10);
        }

        this.storageManager.addOrUpdateFaction(faction);
    }

    public void addClaim(Faction faction, Claim claim)
    {
        faction.addClaim(claim);
        this.storageManager.addOrUpdateFaction(faction);

        World world = Sponge.getServer().getWorld(claim.getWorldUUID()).get();
        Vector3i chunkPosition = claim.getChunkPosition();
        double x = (chunkPosition.getX() << 4) + 8;
        double z = (chunkPosition.getZ() << 4) + 8;
        double y = world.getHighestYAt((int)x, (int)z);
        Vector3d position = new Vector3d(x, y, z);
        world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.CLOUD).quantity(400).offset(new Vector3d(4, 1, 4)).build(), position);
        world.playSound(SoundTypes.ITEM_ARMOR_EQUIP_IRON, position, 5, -10);
    }

    public void removeClaim(Faction faction, Claim claim)
    {
        faction.removeClaim(claim);
        FactionsCache.removeClaimCache(claim);
        this.storageManager.addOrUpdateFaction(faction);
    }

//    public void addClaim(Faction faction, UUID worldUUID, Vector3i claimedChunk)
//    {
//        faction.addClaim(worldUUID.toString() + "|" + claimedChunk.toString());
//
//        storageManager.addOrUpdateFaction(faction);
//    }
//
//    public void removeClaim(Faction faction, UUID worldUUID, Vector3i claimedChunk)
//    {
//        faction.removeClaim(worldUUID.toString() + "|" + claimedChunk.toString());
//
//        storageManager.addOrUpdateFaction(faction);
//    }

    public boolean isClaimed(UUID worldUUID, Vector3i chunk)
    {
        for(Claim claim : getAllClaims())
        {
            if (claim.getWorldUUID().equals(worldUUID) && claim.getChunkPosition().equals(chunk))
                return true;
        }
        return false;
    }

    public boolean isClaimConnected(Faction faction, Claim claimToCheck)
    {
        if (faction.getClaims().size() == 0)
            return true;

        for(Claim claim : faction.getClaims())
        {
            if(!claimToCheck.getWorldUUID().equals(claim.getWorldUUID()))
                continue;

            Vector3i chunkToCheck = claimToCheck.getChunkPosition();
            Vector3i claimChunk = claim.getChunkPosition();

            if((claimChunk.getX() == chunkToCheck.getX()) && ((claimChunk.getZ() + 1 == chunkToCheck.getZ()) || (claimChunk.getZ() - 1 == chunkToCheck.getZ())))
            {
                return true;
            }
            else if((claimChunk.getZ() == chunkToCheck.getZ()) && ((claimChunk.getX() + 1 == chunkToCheck.getX()) || (claimChunk.getX() - 1 == chunkToCheck.getX())))
            {
                return true;
            }
        }
        return false;
    }

//    public boolean isClaimConnected(Faction faction, UUID worldUUID, Vector3i chunk)
//    {
//        Set<String> claimsList = faction.getClaims();
//
//        for(String object : claimsList)
//        {
//            if(object.contains(worldUUID.toString()))
//            {
//                String vectors[] = object.replace(worldUUID.toString() + "|", "").replace("(", "").replace(")", "").replace(" ", "").split(",");
//
//                int x = Integer.valueOf(vectors[0]);
//                int y = Integer.valueOf(vectors[1]);
//                int z = Integer.valueOf(vectors[2]);
//
//                Vector3i claim = Vector3i.from(x, y, z);
//
//                if((claim.getX() == chunk.getX()) && ((claim.getZ() + 1 == chunk.getZ()) || (claim.getZ() - 1 == chunk.getZ())))
//                {
//                    return true;
//                }
//                else if((claim.getZ() == chunk.getZ()) && ((claim.getX() + 1 == chunk.getX()) || (claim.getX() - 1 == chunk.getX())))
//                {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public void setHome(@Nullable UUID worldUUID, Faction faction, @Nullable Vector3i home)
    {
        if(home != null && worldUUID != null)
        {
            faction = faction.toBuilder().setHome(new FactionHome(worldUUID, home)).build();
        }
        else
        {
            faction = faction.toBuilder().setHome(null).build();
        }

        storageManager.addOrUpdateFaction(faction);
    }

    public List<String> getFactionsTags()
    {
        List<Faction> factionsList = new ArrayList<>(getFactions().values());
        List<String> factionsTags = new ArrayList<>();

        for(Faction faction : factionsList)
        {
            factionsTags.add(faction.getTag().toPlain());
        }

        return factionsTags;
    }

    public boolean hasOnlinePlayers(Faction faction)
    {
        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            if(_playerManager.isPlayerOnline(faction.getLeader()))
            {
                return true;
            }
        }

        for(UUID playerUUID : faction.getOfficers())
        {
            if(_playerManager.isPlayerOnline(playerUUID))
            {
                return true;
            }
        }

        for(UUID playerUUID : faction.getMembers())
        {
            if(_playerManager.isPlayerOnline(playerUUID))
            {
                return true;
            }
        }

        for(UUID playerUUID : faction.getRecruits())
        {
            if(_playerManager.isPlayerOnline(playerUUID))
            {
                return true;
            }
        }

        return false;
    }

    public void removeClaims(Faction faction)
    {
        for (Claim claim: faction.getClaims())
        {
            FactionsCache.removeClaimCache(claim);
        }
        faction.removeAllClaims();
        storageManager.addOrUpdateFaction(faction);
    }

    public void kickPlayer(UUID playerUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        if(faction.getRecruits().contains(playerUUID))
        {
            faction.removeRecruit(playerUUID);
        }
        else if(faction.getMembers().contains(playerUUID))
        {
            faction.removeMember(playerUUID);
        }
        else
        {
            faction.removeOfficer(playerUUID);
        }

        storageManager.addOrUpdateFaction(faction);
    }

    private Consumer<Task> addClaimWithDelay(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        return new Consumer<Task>()
        {
            int seconds = 1;

            @Override
            public void accept(Task task)
            {
                if(chunk.toString().equals(player.getLocation().getChunkPosition().toString()))
                {
                    if(seconds >= _configFields.getClaimDelay())
                    {
                        if(_configFields.shouldClaimByItems())
                        {
                            if(addClaimByItems(player, faction, worldUUID, chunk))
                            {
                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
                            }
                        }
                        else
                        {
                            addClaim(faction, new Claim(worldUUID, chunk));
                            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RESET, seconds));
                        seconds++;
                    }
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MOVED_FROM_THE_CHUNK));
                    task.cancel();
                }
            }
        };
    }

    public void startClaiming(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        if(_configFields.shouldDelayClaim())
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.CLAIMING_HAS_BEEN_STARTED + " " + PluginMessages.STAY_IN_THE_CHUNK_FOR + " ", TextColors.GOLD, _configFields.getClaimDelay() + " " + PluginMessages.SECONDS, TextColors.GREEN, " " + PluginMessages.TO_CLAIM_IT));

            Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

            taskBuilder.delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(addClaimWithDelay(player, faction, worldUUID, chunk)).submit(EagleFactions.getPlugin());
        }
        else
        {
            if(_configFields.shouldClaimByItems())
            {
                if(addClaimByItems(player, faction, worldUUID, chunk))
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                addClaim(faction, new Claim(worldUUID, chunk));
            }
        }
    }

    private boolean addClaimByItems(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        HashMap<String, Integer> requiredItems = _configFields.getRequiredItemsToClaim();
        PlayerInventory inventory = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(PlayerInventory.class));
        int allRequiredItems = requiredItems.size();
        int foundItems = 0;

        for(String requiredItem : requiredItems.keySet())
        {
            String[] idAndVariant = requiredItem.split(":");

            String itemId = idAndVariant[0] + ":" + idAndVariant[1];
            Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

            if(itemType.isPresent())
            {
                ItemStack itemStack = ItemStack.builder()
                        .itemType(itemType.get()).build();
                itemStack.setQuantity(requiredItems.get(requiredItem));

                if(idAndVariant.length == 3)
                {
                    if(itemType.get().getBlock().isPresent())
                    {
                        int variant = Integer.parseInt(idAndVariant[2]);
                        BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                        itemStack = ItemStack.builder().fromBlockState(blockState).build();
                    }
                }

                if(inventory.contains(itemStack))
                {
                    foundItems += 1;
                }
                else
                {
                    return false;
                }
            }
        }

        if(allRequiredItems == foundItems)
        {
            for(String requiredItem : requiredItems.keySet())
            {
                String[] idAndVariant = requiredItem.split(":");
                String itemId = idAndVariant[0] + ":" + idAndVariant[1];

                Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

                if(itemType.isPresent())
                {
                    ItemStack itemStack = ItemStack.builder()
                            .itemType(itemType.get()).build();
                    itemStack.setQuantity(requiredItems.get(requiredItem));

                    if(idAndVariant.length == 3)
                    {
                        if(itemType.get().getBlock().isPresent())
                        {
                            int variant = Integer.parseInt(idAndVariant[2]);
                            BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                            itemStack = ItemStack.builder().fromBlockState(blockState).build();
                        }
                    }

                    inventory.query(QueryOperationTypes.ITEM_TYPE.of(itemType.get())).poll(itemStack.getQuantity());
                }
            }

            addClaim(faction, new Claim(worldUUID, chunk));
            return true;
        }
        else
        {
            return false;
        }
    }

    public void toggleFlag(Faction faction, FactionMemberType factionMemberType, FactionFlagTypes factionFlagTypes, Boolean flagValue)
    {
        faction.setFlag(factionMemberType, factionFlagTypes, flagValue);

        storageManager.addOrUpdateFaction(faction);
    }

    public void changeTagColor(Faction faction, TextColor textColor)
    {
        Text text = Text.of(textColor, faction.getTag().toPlainSingle());
        faction.setTag(text);

        storageManager.addOrUpdateFaction(faction);
    }

    public void addMemberAndRemoveRecruit(UUID newMemberUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.addMember(newMemberUUID);
        faction.removeRecruit(newMemberUUID);

        storageManager.addOrUpdateFaction(faction);
    }

    public void addRecruitAndRemoveMember(UUID newRecruitUUID, String factionName)
    {
        Faction faction = getFactionByName(factionName);

        faction.addRecruit(newRecruitUUID);
        faction.removeMember(newRecruitUUID);

        storageManager.addOrUpdateFaction(faction);
    }

    public FactionMemberType promotePlayer(Faction faction, Player playerToPromote)
    {
        FactionMemberType promotedTo = FactionMemberType.RECRUIT;

        if(faction.getRecruits().contains(playerToPromote.getUniqueId()))
        {
            faction.getMembers().add(playerToPromote.getUniqueId());
            faction.getRecruits().remove(playerToPromote.getUniqueId());
            promotedTo = FactionMemberType.MEMBER;
        }
        else if (faction.getMembers().contains(playerToPromote.getUniqueId()))
        {
            faction.getOfficers().add(playerToPromote.getUniqueId());
            faction.getMembers().remove(playerToPromote.getUniqueId());
            promotedTo = FactionMemberType.OFFICER;
        }

        this.storageManager.addOrUpdateFaction(faction);
        return promotedTo;
    }

    public FactionMemberType demotePlayer(Faction faction, Player playerToDemote)
    {
        FactionMemberType demotedTo = FactionMemberType.RECRUIT;

        if(faction.getMembers().contains(playerToDemote.getUniqueId()))
        {
            faction.getRecruits().add(playerToDemote.getUniqueId());
            faction.getMembers().remove(playerToDemote.getUniqueId());
            demotedTo = FactionMemberType.RECRUIT;
        }
        else if (faction.getOfficers().contains(playerToDemote.getUniqueId()))
        {
            faction.getMembers().add(playerToDemote.getUniqueId());
            faction.getOfficers().remove(playerToDemote.getUniqueId());
            demotedTo = FactionMemberType.MEMBER;
        }

        this.storageManager.addOrUpdateFaction(faction);
        return demotedTo;
    }

    public void setLastOnline(Faction faction, Instant instantTime)
    {
        faction.setLastOnline(instantTime);
        this.storageManager.addOrUpdateFaction(faction);
    }

    public void renameFaction(Faction faction, String newFactionName)
    {
        this.storageManager.deleteFaction(faction.getName());
        faction = faction.toBuilder().setName(newFactionName).build();
        this.storageManager.addOrUpdateFaction(faction);
    }

    public void changeTag(Faction faction, String newTag)
    {
        faction = faction.toBuilder().setTag(Text.of(faction.getTag().getColor(), newTag)).build();
        this.storageManager.addOrUpdateFaction(faction);
    }

    public void setChest(Faction faction, FactionChest inventory)
    {
        faction = faction.toBuilder().setChest(inventory).build();
        this.storageManager.addOrUpdateFaction(faction);
    }
}
