package io.github.aquerr.eaglefactions.common.logic;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Strings;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import io.github.aquerr.eaglefactions.common.scheduling.ClaimDelayTask;
import io.github.aquerr.eaglefactions.common.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.common.util.ParticlesUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogicImpl implements FactionLogic
{
    private final StorageManager storageManager;
    private final FactionsConfig factionsConfig;
    private final PlayerManager playerManager;

    private final UUID DUMMY_UUID = new UUID(0, 0);

    public FactionLogicImpl(final PlayerManager playerManager, final StorageManager storageManager, final FactionsConfig factionsConfig)
    {
        this.storageManager = storageManager;
        this.playerManager = playerManager;
        this.factionsConfig = factionsConfig;
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
        return storageManager.getFaction(factionName);
    }

    @Override
    public List<Player> getOnlinePlayers(Faction faction)
    {
        final List<Player> factionPlayers = new ArrayList<>();
        final UUID factionLeader = faction.getLeader();
        if(!faction.getLeader().equals(DUMMY_UUID) && playerManager.isPlayerOnline(factionLeader))
        {
            factionPlayers.add(playerManager.getPlayer(factionLeader).get());
        }

        for(final UUID uuid : faction.getOfficers())
        {
            if(playerManager.isPlayerOnline(uuid))
            {
                factionPlayers.add(playerManager.getPlayer(uuid).get());
            }
        }

        for(final UUID uuid : faction.getMembers())
        {
            if(playerManager.isPlayerOnline(uuid))
            {
                factionPlayers.add(playerManager.getPlayer(uuid).get());
            }
        }

        for(final UUID uuid : faction.getRecruits())
        {
            if(playerManager.isPlayerOnline(uuid))
            {
                factionPlayers.add(playerManager.getPlayer(uuid).get());
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
        Objects.requireNonNull(factionName);

        final boolean isDisbanded = this.storageManager.deleteFaction(factionName);
        final List<Faction> tempFactions = new ArrayList<>(getFactions().values());
        for (final Faction faction : tempFactions)
        {
            if (faction.getTruces().contains(factionName))
                removeTruce(faction.getName(), factionName);
            else if (faction.getAlliances().contains(factionName))
                removeAlly(faction.getName(), factionName);
            else if (faction.getEnemies().contains(factionName))
                removeEnemy(faction.getName(), factionName);
        }
        return isDisbanded;
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
    public void addTruce(String playerFactionName, String invitedFactionName)
    {
        if(Strings.isNullOrEmpty(playerFactionName) || Strings.isNullOrEmpty(invitedFactionName))
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction invitedFaction = getFactionByName(invitedFactionName);

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getTruces());
        final Set<String> invitedFactionAlliances = new HashSet<>(invitedFaction.getTruces());

        playerFactionAlliances.add(invitedFactionName);
        invitedFactionAlliances.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setTruces(playerFactionAlliances).build();
        final Faction updatedInvitedFaction = invitedFaction.toBuilder().setTruces(invitedFactionAlliances).build();

        storageManager.addOrUpdateFaction(updatedPlayerFaction);
        storageManager.addOrUpdateFaction(updatedInvitedFaction);
    }

    @Override
    public void removeTruce(String playerFactionName, String removedFactionName)
    {
        if(Strings.isNullOrEmpty(playerFactionName) || Strings.isNullOrEmpty(removedFactionName))
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction removedFaction = getFactionByName(removedFactionName);

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getTruces());
        final Set<String> removedFactionAlliances = new HashSet<>(removedFaction.getTruces());

        playerFactionAlliances.remove(removedFactionName);
        removedFactionAlliances.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setTruces(playerFactionAlliances).build();
        final Faction updatedRemovedFaction = removedFaction.toBuilder().setTruces(removedFactionAlliances).build();

        storageManager.addOrUpdateFaction(updatedPlayerFaction);
        storageManager.addOrUpdateFaction(updatedRemovedFaction);
    }

    @Override
    public void addAlly(String playerFactionName, String invitedFactionName)
    {
        if(Strings.isNullOrEmpty(playerFactionName) || Strings.isNullOrEmpty(invitedFactionName))
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");

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
        if(Strings.isNullOrEmpty(playerFactionName) || Strings.isNullOrEmpty(removedFactionName))
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");

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
        if(Strings.isNullOrEmpty(playerFactionName) || Strings.isNullOrEmpty(enemyFactionName))
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");

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
        if(Strings.isNullOrEmpty(playerFactionName) || Strings.isNullOrEmpty(enemyFactionName))
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");

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
        if (faction == null)
            return;

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
        }
        else if(faction.getMembers().contains(newLeaderUUID))
        {
            members.remove(newLeaderUUID);
        }
        else if(faction.getRecruits().contains(newLeaderUUID))
        {
            recruits.remove(newLeaderUUID);
        }

        final Faction updatedFaction = faction.toBuilder()
                .setLeader(newLeaderUUID)
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
            ParticlesUtil.spawnClaimParticles(claim);
        }

        final Faction updatedFaction = faction.toBuilder().setClaims(factionClaims).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void addClaim(final Faction faction, final Claim claim)
    {
        final Set<Claim> claims = new HashSet<>(faction.getClaims());
        claims.add(claim);
        final Faction updatedFaction = faction.toBuilder().setClaims(claims).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);

		ParticlesUtil.spawnClaimParticles(claim);
    }

    @Override
    public void removeClaim(final Faction faction, final Claim claim)
    {
        removeClaimInternal(faction, claim);
		ParticlesUtil.spawnUnclaimParticles(claim);
    }

    @Override
    public void destroyClaim(final Faction faction, final Claim claim)
    {
        removeClaimInternal(faction, claim);
        ParticlesUtil.spawnDestroyClaimParticles(claim);
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
    public void setHome(Faction faction, @Nullable FactionHome home)
    {
        if (home != null && home.getBlockPosition() != null && home.getWorldUUID() != null)
        {
            faction = faction.toBuilder().setHome(home).build();
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
            if(playerManager.isPlayerOnline(faction.getLeader()))
            {
                return true;
            }
        }

        for(UUID playerUUID : faction.getOfficers())
        {
            if(playerManager.isPlayerOnline(playerUUID))
            {
                return true;
            }
        }

        for(UUID playerUUID : faction.getMembers())
        {
            if(playerManager.isPlayerOnline(playerUUID))
            {
                return true;
            }
        }

        for(UUID playerUUID : faction.getRecruits())
        {
            if(playerManager.isPlayerOnline(playerUUID))
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
    public void startClaiming(Player player, Faction faction, UUID worldUUID, Vector3i chunkPosition)
    {
        if(this.factionsConfig.shouldDelayClaim())
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.STAY_IN_THE_CHUNK_FOR_NUMBER_SECONDS_TO_CLAIM_IT, Collections.singletonMap(Placeholders.NUMBER, Text.of(TextColors.GOLD, this.factionsConfig.getClaimDelay())))));
            EagleFactionsScheduler.getInstance().scheduleWithDelayedInterval(new ClaimDelayTask(player, chunkPosition), 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
        }
        else
        {
            if(this.factionsConfig.shouldClaimByItems())
            {
                boolean didSucceed = addClaimByItems(player, faction, worldUUID, chunkPosition);
                if(didSucceed)
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunkPosition.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
                else
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunkPosition.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
                addClaim(faction, new Claim(worldUUID, chunkPosition));
            }
        }
    }

    @Override
    public boolean addClaimByItems(Player player, Faction faction, UUID worldUUID, Vector3i chunkPosition)
    {
        Map<String, Integer> requiredItems = this.factionsConfig.getRequiredItemsToClaim();
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

            addClaim(faction, new Claim(worldUUID, chunkPosition));
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void toggleFlag(Faction faction, FactionMemberType factionMemberType, FactionPermType factionPermType, Boolean flagValue)
    {
        final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms = new HashMap<>(faction.getPerms());
        perms.get(factionMemberType).replace(factionPermType, flagValue);

        final Faction updatedFaction = faction.toBuilder().setPerms(perms).build();
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
    public FactionMemberType promotePlayer(Faction faction, UUID playerToPromote)
    {
        FactionMemberType promotedTo = FactionMemberType.RECRUIT;

        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());

        if(recruits.contains(playerToPromote))
        {
            members.add(playerToPromote);
            recruits.remove(playerToPromote);
            promotedTo = FactionMemberType.MEMBER;
        }
        else if (members.contains(playerToPromote))
        {
            officers.add(playerToPromote);
            members.remove(playerToPromote);
            promotedTo = FactionMemberType.OFFICER;
        }

        final Faction updatedFaction = faction.toBuilder().setRecruits(recruits).setOfficers(officers).setMembers(members).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
        return promotedTo;
    }

    @Override
    public FactionMemberType demotePlayer(Faction faction, UUID playerToDemote)
    {
        FactionMemberType demotedTo = FactionMemberType.RECRUIT;
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());

        if(members.contains(playerToDemote))
        {
            recruits.add(playerToDemote);
            members.remove(playerToDemote);
            demotedTo = FactionMemberType.RECRUIT;
        }
        else if (officers.contains(playerToDemote))
        {
            members.add(playerToDemote);
            officers.remove(playerToDemote);
            demotedTo = FactionMemberType.MEMBER;
        }

        final Faction updatedFaction = faction.toBuilder().setRecruits(recruits).setOfficers(officers).setMembers(members).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
        return demotedTo;
    }

    @Override
    public void setLastOnline(final Faction faction, final Instant instantTime)
    {
        final Faction updatedFaction = faction.toBuilder().setLastOnline(instantTime).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void renameFaction(final Faction faction, final String newFactionName)
    {
        this.storageManager.deleteFaction(faction.getName());
        Faction updatedFaction = faction.toBuilder().setName(newFactionName).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void changeTag(final Faction faction, final String newTag)
    {
        Faction updatedFaction = faction.toBuilder().setTag(Text.of(faction.getTag().getColor(), newTag)).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    @Override
    public void setChest(final Faction faction, final FactionChest inventory)
    {
        Faction updatedFaction = faction.toBuilder().setChest(inventory).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
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
    public void setIsPublic(final Faction faction, final boolean isPublic)
    {
        final Faction updatedFaction = faction.toBuilder().setIsPublic(isPublic).build();
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }

    private void removeClaimInternal(final Faction faction, final Claim claim)
    {
        final Set<Claim> claims = new HashSet<>(faction.getClaims());
        claims.remove(claim);
        final Faction updatedFaction = faction.toBuilder().setClaims(claims).build();
        FactionsCache.removeClaimCache(claim);
        this.storageManager.addOrUpdateFaction(updatedFaction);
    }
}
