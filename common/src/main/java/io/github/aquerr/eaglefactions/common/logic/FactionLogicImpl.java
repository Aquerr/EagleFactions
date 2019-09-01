package io.github.aquerr.eaglefactions.common.logic;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.managers.PlayerManager;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import io.github.aquerr.eaglefactions.common.scheduling.ClaimDelayTask;
import io.github.aquerr.eaglefactions.common.scheduling.EagleFactionsScheduler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogicImpl implements FactionLogic
{
    private static FactionLogicImpl INSTANCE = null;

    private final StorageManager storageManager;
    private final ConfigFields _configFields;
    private final PlayerManager _playerManager;
    private final EagleFactionsPlugin plugin;

    private final UUID DUMMY_UUID = new UUID(0, 0);

    public static FactionLogic getInstance(final EagleFactionsPlugin eagleFactions)
    {
        if (INSTANCE == null)
            return new FactionLogicImpl(eagleFactions);
        else return INSTANCE;
    }

    public FactionLogicImpl(EagleFactionsPlugin plugin)
    {
        INSTANCE = this;
        this.plugin = plugin;
        _configFields = plugin.getConfiguration().getConfigFields();
        _playerManager = plugin.getPlayerManager();
        this.storageManager = plugin.getStorageManager();
    }

    @Override
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

    @Override
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

    @Override
    public @Nullable Faction getFactionByName(String factionName)
    {
        Faction faction = storageManager.getFaction(factionName);

        if(faction != null)
        {
            return faction;
        }

        return null;
    }

    @Override
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

    @Override
    public Set<String> getFactionsNames()
    {
        return getFactions().keySet();
    }

    @Override
    public Map<String, Faction> getFactions()
    {
        return FactionsCache.getFactionsMap();
    }

    @Override
    public void addFaction(Faction faction)
    {
        storageManager.addOrUpdateFaction(faction);
    }

    @Override
    public boolean disbandFaction(String factionName)
    {
        return this.storageManager.deleteFaction(factionName);
    }

    @Override
    public void joinFaction(UUID playerUUID, String factionName)
    {
        if(playerUUID == null || factionName.equals(""))
        {
            throw new IllegalArgumentException("playerUUID can't be null and/or factionName can't be empty.");
        }

        Faction faction = getFactionByName(factionName);
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        recruits.add(playerUUID);
        Faction updatedFaction = faction.toBuilder().setRecruits(recruits).build();
        storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void leaveFaction(UUID playerUUID, String factionName)
    {
        final Faction faction = getFactionByName(factionName);
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());

        if(faction.getRecruits().contains(playerUUID))
        {
            recruits.remove(playerUUID);
        }
        else if(faction.getMembers().contains(playerUUID))
        {
            members.remove(playerUUID);
        }
        else
        {
            officers.remove(playerUUID);
        }

        final Faction updatedFaction = faction.toBuilder().setRecruits(recruits).setMembers(members).setOfficers(officers).build();
        storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void addAlly(String playerFactionName, String invitedFactionName)
    {
        if(playerFactionName == null || invitedFactionName == null || playerFactionName.equals("") || invitedFactionName.equals(""))
        {
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");
        }

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction invitedFaction = getFactionByName(invitedFactionName);

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getAlliances());
        final Set<String> invitedFactionAlliances = new HashSet<>(invitedFaction.getAlliances());

        playerFactionAlliances.add(invitedFactionName);
        invitedFactionAlliances.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setAlliances(playerFactionAlliances).build();
        final Faction updatedInvitedFaction = invitedFaction.toBuilder().setAlliances(invitedFactionAlliances).build();

        storageManager.addOrUpdateFaction(updatedPlayerFaction);
        storageManager.addOrUpdateFaction(updatedInvitedFaction);
    }

    @Override
    public void removeAlly(String playerFactionName, String removedFactionName)
    {
        if(playerFactionName == null || removedFactionName == null || playerFactionName.equals("") || removedFactionName.equals(""))
        {
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");
        }

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction removedFaction = getFactionByName(removedFactionName);

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getAlliances());
        final Set<String> removedFactionAlliances = new HashSet<>(removedFaction.getAlliances());

        playerFactionAlliances.remove(removedFactionName);
        removedFactionAlliances.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setAlliances(playerFactionAlliances).build();
        final Faction updatedRemovedFaction = removedFaction.toBuilder().setAlliances(removedFactionAlliances).build();

        storageManager.addOrUpdateFaction(updatedPlayerFaction);
        storageManager.addOrUpdateFaction(updatedRemovedFaction);
    }

    @Override
    public void addEnemy(String playerFactionName, String enemyFactionName)
    {
        if(playerFactionName == null || enemyFactionName == null || playerFactionName.equals("") || enemyFactionName.equals(""))
        {
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");
        }

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction enemyFaction = getFactionByName(enemyFactionName);

        final Set<String> playerFactionEnemies = new HashSet<>(playerFaction.getEnemies());
        final Set<String> enemyFactionEnemies = new HashSet<>(enemyFaction.getEnemies());

        playerFactionEnemies.add(enemyFactionName);
        enemyFactionEnemies.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setEnemies(playerFactionEnemies).build();
        final Faction updatedEnemyFaction = enemyFaction.toBuilder().setEnemies(enemyFactionEnemies).build();

        storageManager.addOrUpdateFaction(updatedPlayerFaction);
        storageManager.addOrUpdateFaction(updatedEnemyFaction);
    }

    @Override
    public void removeEnemy(String playerFactionName, String enemyFactionName)
    {
        if(playerFactionName == null || enemyFactionName == null || playerFactionName.equals("") || enemyFactionName.equals(""))
        {
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");
        }

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction enemyFaction = getFactionByName(enemyFactionName);

        final Set<String> playerFactionEnemies = new HashSet<>(playerFaction.getEnemies());
        final Set<String> enemyFactionEnemies = new HashSet<>(enemyFaction.getEnemies());

        playerFactionEnemies.remove(enemyFactionName);
        enemyFactionEnemies.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setEnemies(playerFactionEnemies).build();
        final Faction updatedEnemyFaction = enemyFaction.toBuilder().setEnemies(enemyFactionEnemies).build();

        storageManager.addOrUpdateFaction(updatedPlayerFaction);
        storageManager.addOrUpdateFaction(updatedEnemyFaction);
    }

    @Override
    public void setLeader(UUID newLeaderUUID, String playerFactionName)
    {
        final Faction faction = getFactionByName(playerFactionName);
        UUID leader = faction.getLeader();
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());

        if(!faction.getLeader().equals(DUMMY_UUID))
        {
            officers.add(faction.getLeader());
        }

        if(faction.getOfficers().contains(newLeaderUUID))
        {
            officers.remove(newLeaderUUID);
            leader = newLeaderUUID;
        }
        else if(faction.getMembers().contains(newLeaderUUID))
        {
            members.remove(newLeaderUUID);
            leader = newLeaderUUID;
        }
        else if(faction.getRecruits().contains(newLeaderUUID))
        {
            recruits.remove(newLeaderUUID);
            leader = newLeaderUUID;
        }

        final Faction updatedFaction = faction.toBuilder()
                .setLeader(leader)
                .setOfficers(officers)
                .setMembers(members)
                .setRecruits(recruits)
                .build();

        storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public Set<Claim> getAllClaims()
    {
        return FactionsCache.getAllClaims();
    }

    @Override
    public void addClaims(final Faction faction, final List<Claim> claims)
    {
        final Set<Claim> factionClaims = new HashSet<>(faction.getClaims());

        for(final Claim claim : claims)
        {
            factionClaims.add(claim);
            final World world = Sponge.getServer().getWorld(claim.getWorldUUID()).get();
            final Vector3i chunkPosition = claim.getChunkPosition();
            final double x = (chunkPosition.getX() << 4) + 8;
            final double z = (chunkPosition.getZ() << 4) + 8;
            final double y = world.getHighestYAt((int)x, (int)z);
            final Vector3d position = new Vector3d(x, y, z);
            world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.CLOUD).quantity(400).offset(new Vector3d(4, 1, 4)).build(), position);
            world.playSound(SoundTypes.ITEM_ARMOR_EQUIP_IRON, position, 5, -10);
        }

        final Faction updatedFaction = faction.toBuilder().setClaims(factionClaims).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void addClaim(Faction faction, Claim claim)
    {
        final Set<Claim> claims = new HashSet<>(faction.getClaims());
        claims.add(claim);
        final Faction updatedFaction = faction.toBuilder().setClaims(claims).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);

        World world = Sponge.getServer().getWorld(claim.getWorldUUID()).get();
        Vector3i chunkPosition = claim.getChunkPosition();
        double x = (chunkPosition.getX() << 4) + 8;
        double z = (chunkPosition.getZ() << 4) + 8;
        double y = world.getHighestYAt((int)x, (int)z);
        Vector3d position = new Vector3d(x, y, z);
        world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.CLOUD).quantity(400).offset(new Vector3d(4, 1, 4)).build(), position);
        world.playSound(SoundTypes.ITEM_ARMOR_EQUIP_IRON, position, 5, -10);
    }

    @Override
    public void removeClaim(Faction faction, Claim claim)
    {
        final Set<Claim> claims = new HashSet<>(faction.getClaims());
        claims.remove(claim);
        final Faction updatedFaction = faction.toBuilder().setClaims(claims).build();
        FactionsCache.removeClaimCache(claim);
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public boolean isClaimed(UUID worldUUID, Vector3i chunk)
    {
        for(Claim claim : getAllClaims())
        {
            if (claim.getWorldUUID().equals(worldUUID) && claim.getChunkPosition().equals(chunk))
                return true;
        }
        return false;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void removeAllClaims(Faction faction)
    {
        for (Claim claim: faction.getClaims())
        {
            FactionsCache.removeClaimCache(claim);
        }
        final Faction updatedFaction = faction.toBuilder().setClaims(new HashSet<>()).build();
        storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void kickPlayer(UUID playerUUID, String factionName)
    {
        final Faction faction = getFactionByName(factionName);
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());

        if(faction.getRecruits().contains(playerUUID))
        {
            recruits.remove(playerUUID);
        }
        else if(faction.getMembers().contains(playerUUID))
        {
            members.remove(playerUUID);
        }
        else
        {
            officers.remove(playerUUID);
        }
        final Faction updatedFaction = faction.toBuilder()
                .setOfficers(officers)
                .setMembers(members)
                .setRecruits(recruits)
                .build();
        storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void startClaiming(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        if(_configFields.shouldDelayClaim())
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.CLAIMING_HAS_BEEN_STARTED + " " + PluginMessages.STAY_IN_THE_CHUNK_FOR + " ", TextColors.GOLD, _configFields.getClaimDelay() + " " + PluginMessages.SECONDS, TextColors.GREEN, " " + PluginMessages.TO_CLAIM_IT));
            EagleFactionsScheduler.getInstance().scheduleWithDelayedInterval(new ClaimDelayTask(player, chunk), 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
//            taskBuilder.delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(addClaimWithDelay(player, faction, worldUUID, chunk)).submit(EagleFactionsPlugin.getPlugin());
        }
        else
        {
            if(_configFields.shouldClaimByItems())
            {
                boolean didSucceed = addClaimByItems(player, faction, worldUUID, chunk);
                if(didSucceed)
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                else
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                addClaim(faction, new Claim(worldUUID, chunk));
            }
        }
    }

    @Override
    public boolean addClaimByItems(Player player, Faction faction, UUID worldUUID, Vector3i chunk)
    {
        Map<String, Integer> requiredItems = _configFields.getRequiredItemsToClaim();
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

    @Override
    public void toggleFlag(Faction faction, FactionMemberType factionMemberType, FactionFlagTypes factionFlagTypes, Boolean flagValue)
    {
        final Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags = new HashMap<>(faction.getFlags());
        flags.get(factionMemberType).replace(factionFlagTypes, flagValue);

        final Faction updatedFaction = faction.toBuilder().setFlags(flags).build();
        storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void changeTagColor(Faction faction, TextColor textColor)
    {
        Text text = Text.of(textColor, faction.getTag().toPlainSingle());
        final Faction updatedFaction = faction.toBuilder().setTag(text).build();
        storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public FactionMemberType promotePlayer(Faction faction, Player playerToPromote)
    {
        FactionMemberType promotedTo = FactionMemberType.RECRUIT;

        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());

        if(recruits.contains(playerToPromote.getUniqueId()))
        {
            members.add(playerToPromote.getUniqueId());
            recruits.remove(playerToPromote.getUniqueId());
            promotedTo = FactionMemberType.MEMBER;
        }
        else if (members.contains(playerToPromote.getUniqueId()))
        {
            officers.add(playerToPromote.getUniqueId());
            members.remove(playerToPromote.getUniqueId());
            promotedTo = FactionMemberType.OFFICER;
        }

        final Faction updatedFaction = faction.toBuilder().setRecruits(recruits).setOfficers(officers).setMembers(members).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
        return promotedTo;
    }

    @Override
    public FactionMemberType demotePlayer(Faction faction, Player playerToDemote)
    {
        FactionMemberType demotedTo = FactionMemberType.RECRUIT;
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());

        if(members.contains(playerToDemote.getUniqueId()))
        {
            recruits.add(playerToDemote.getUniqueId());
            members.remove(playerToDemote.getUniqueId());
            demotedTo = FactionMemberType.RECRUIT;
        }
        else if (officers.contains(playerToDemote.getUniqueId()))
        {
            members.add(playerToDemote.getUniqueId());
            officers.remove(playerToDemote.getUniqueId());
            demotedTo = FactionMemberType.MEMBER;
        }

        final Faction updatedFaction = faction.toBuilder().setRecruits(recruits).setOfficers(officers).setMembers(members).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
        return demotedTo;
    }

    @Override
    public void setLastOnline(Faction faction, Instant instantTime)
    {
        final Faction updatedFaction = faction.toBuilder().setLastOnline(instantTime).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void renameFaction(Faction faction, String newFactionName)
    {
        this.storageManager.deleteFaction(faction.getName());
        faction = faction.toBuilder().setName(newFactionName).build();
        this.storageManager.addOrUpdateFaction(faction);
    }

    @Override
    public void changeTag(Faction faction, String newTag)
    {
        faction = faction.toBuilder().setTag(Text.of(faction.getTag().getColor(), newTag)).build();
        this.storageManager.addOrUpdateFaction(faction);
    }

    @Override
    public void setChest(Faction faction, FactionChest inventory)
    {
        faction = faction.toBuilder().setChest(inventory).build();
        this.storageManager.addOrUpdateFaction(faction);
    }

    @Override
    public void setDescription(final Faction faction, final String description)
    {
        final Faction updatedFaction = faction.toBuilder().setDescription(description).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void setMessageOfTheDay(final Faction faction, final String motd)
    {
        final Faction updatedFaction = faction.toBuilder().setMessageOfTheDay(motd).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public Inventory convertFactionChestToInventory(final FactionChest factionChest)
    {
        Inventory inventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.BLUE, Text.of("Faction's chest"))))
                .listener(InteractInventoryEvent.Close.class, new FactionChestCloseListener(this.plugin, factionChest.getFactionName()))
                .build(EagleFactionsPlugin.getPlugin());

        //Fill it with items
        int column = 1;
        int row = 1;

        for(final Inventory slot : inventory.slots())
        {
            ItemStack itemStack = null;
            for(final FactionChest.SlotItem slotItem : factionChest.getItems())
            {
                if(slotItem.getRow() == row && slotItem.getColumn() == column)
                    itemStack = ItemStack.builder().fromContainer(slotItem.getItem().toContainer()).build();
            }

            if(itemStack != null)
                slot.offer(itemStack);

            column++;
            if(column > 9)
            {
                column = 1;
                row++;
            }
        }

        return inventory;
    }
}
