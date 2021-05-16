package io.github.aquerr.eaglefactions.common.logic;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import io.github.aquerr.eaglefactions.common.scheduling.ClaimDelayTask;
import io.github.aquerr.eaglefactions.common.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.common.util.ItemUtil;
import io.github.aquerr.eaglefactions.common.util.ParticlesUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogicImpl implements FactionLogic
{
    private final StorageManager storageManager;
    private final FactionsConfig factionsConfig;
    private final PlayerManager playerManager;

    public FactionLogicImpl(final PlayerManager playerManager, final StorageManager storageManager, final FactionsConfig factionsConfig)
    {
        this.storageManager = storageManager;
        this.playerManager = playerManager;
        this.factionsConfig = factionsConfig;
    }

    @Override
    public Optional<Faction> getFactionByPlayerUUID(UUID playerUUID)
    {
        checkNotNull(playerUUID);

        //TODO: Theoretically, we could get faction directly from the player... but... let's test it before...
        return this.playerManager.getFactionPlayer(playerUUID)
                .flatMap(FactionPlayer::getFactionName)
                .map(this::getFactionByName);
    }

    @Override
    public Optional<Faction> getFactionByChunk(final UUID worldUUID, final Vector3i chunk)
    {
        checkNotNull(worldUUID);
        checkNotNull(chunk);

        Claim claim = new Claim(worldUUID, chunk);

        Optional<Faction> cachedOptional = FactionsCache.getClaimFaction(claim);
        //noinspection OptionalAssignedToNull
        if (cachedOptional != null) return cachedOptional;

        for(Faction faction : getFactions().values())
        {
            if(faction.getClaims().contains(claim))
            {
                FactionsCache.updateClaimFaction(claim, Optional.of(faction));
                return Optional.of(faction);
            }
        }

        FactionsCache.updateClaimFaction(claim, Optional.empty());
        return Optional.empty();
    }

    @Override
    public @Nullable Faction getFactionByName(String factionName)
    {
        Validate.notBlank(factionName);
        return storageManager.getFaction(factionName);
    }

    @Override
    public List<Player> getOnlinePlayers(final Faction faction)
    {
        checkNotNull(faction);

        final List<Player> factionPlayers = new ArrayList<>();
        final UUID factionLeader = faction.getLeader();
        if(!faction.getLeader().equals(new UUID(0, 0)) && this.playerManager.isPlayerOnline(factionLeader))
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
        return new HashMap<>(FactionsCache.getFactionsMap());
    }

    @Override
    public Map<Claim, Optional<Faction>> getAllClaims()
    {
        return new HashMap<>(FactionsCache.getClaims());
    }

    @Override
    public void addFaction(final Faction faction)
    {
        checkNotNull(faction);
        storageManager.saveFaction(faction);
    }

    @Override
    public boolean disbandFaction(final String factionName)
    {
        checkNotNull(factionName);

        final Faction factionToDisband = this.storageManager.getFaction(factionName);

        Preconditions.checkNotNull(factionToDisband, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), factionName));

        //Update players...
        CompletableFuture.runAsync(() -> {
            final Set<UUID> playerUUIDs = factionToDisband.getPlayers();
            for (final UUID playerUUID : playerUUIDs)
            {
                //Faction Player should always exists so we do not need to check if it is present.
                final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(playerUUID).get();
                final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), null, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
                this.storageManager.savePlayer(updatedPlayer);
            }
        });

        // Update other factions
        CompletableFuture.runAsync(() -> {
            final Set<String> alliances = factionToDisband.getAlliances();
            final Set<String> truces = factionToDisband.getTruces();
            final Set<String> enemies = factionToDisband.getEnemies();
            for (final String alliance : alliances)
            {
                removeAlly(alliance, factionToDisband.getName());
            }
            for (final String truce : truces)
            {
                removeTruce(truce, factionToDisband.getName());
            }
            for (final String enemy : enemies)
            {
                removeEnemy(enemy, factionToDisband.getName());
            }
        });
        return this.storageManager.deleteFaction(factionName);
    }

    @Override
    public void leaveFaction(final UUID playerUUID, final String factionName)
    {
        checkNotNull(playerUUID);
        checkNotNull(factionName);

        final Faction faction = getFactionByName(factionName);

        checkNotNull(faction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), factionName));

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

        //Remove player from claim owners
        final Set<Claim> updatedClaims = new HashSet<>();
        for (final Claim claim : faction.getClaims()) {
            final Set<UUID> owners = new HashSet<>(claim.getOwners());
            owners.remove(playerUUID);
            final Claim updatedClaim = new Claim(claim.getWorldUUID(), claim.getChunkPosition(), owners, claim.isAccessibleByFaction());
            updatedClaims.add(updatedClaim);
        }

        final Faction updatedFaction = faction.toBuilder().setRecruits(recruits).setMembers(members).setOfficers(officers).setClaims(updatedClaims).build();
        storageManager.saveFaction(updatedFaction);

        //Save player...
        final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(playerUUID).get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), null, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        this.storageManager.savePlayer(updatedPlayer);
    }

    @Override
    public void setFaction(UUID playerUUID, String factionName, FactionMemberType rank)
    {
        checkNotNull(playerUUID);
        checkNotNull(factionName);
        checkNotNull(rank);

        // Delete player in old faction
        getFactionByPlayerUUID(playerUUID)
                .ifPresent((faction) -> {
                    if (playerUUID.equals(faction.getLeader()))
                    {
                        final Faction updatedFaction = faction.toBuilder()
                                .setLeader(new UUID(0, 0))
                                .build();

                        storageManager.saveFaction(updatedFaction);
                    }
                    else
                    {
                        leaveFaction(playerUUID, factionName);
                    }
                });

        Faction faction = getFactionByName(factionName);
        checkNotNull(faction);

        final Set<UUID> officers = new HashSet<>(faction.getOfficers());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());

        Faction.Builder factionBuilder = faction.toBuilder();

        switch (rank)
        {
            case LEADER:
            {
                final UUID leaderUUID = faction.getLeader();
                if (leaderUUID != new UUID(0, 0))
                {
                    officers.add(leaderUUID);
                }
                factionBuilder.setLeader(playerUUID);
                break;
            }
            case OFFICER:
            {
                officers.add(playerUUID);
                factionBuilder.setOfficers(officers);
                break;
            }
            case MEMBER:
            {
                members.add(playerUUID);
                factionBuilder.setMembers(members);
                break;
            }
            case RECRUIT:
            {
                recruits.add(playerUUID);
                factionBuilder.setRecruits(recruits);
                break;
            }
            default:
        }

        storageManager.saveFaction(factionBuilder.build());
    }

    @Override
    public void addTruce(final String playerFactionName, final String invitedFactionName)
    {
        Validate.notBlank(playerFactionName);
        Validate.notBlank(invitedFactionName);

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction invitedFaction = getFactionByName(invitedFactionName);

        checkNotNull(playerFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), playerFactionName));
        checkNotNull(invitedFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), invitedFactionName));

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getTruces());
        final Set<String> invitedFactionAlliances = new HashSet<>(invitedFaction.getTruces());

        playerFactionAlliances.add(invitedFactionName);
        invitedFactionAlliances.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setTruces(playerFactionAlliances).build();
        final Faction updatedInvitedFaction = invitedFaction.toBuilder().setTruces(invitedFactionAlliances).build();

        storageManager.saveFaction(updatedPlayerFaction);
        storageManager.saveFaction(updatedInvitedFaction);
    }

    @Override
    public void removeTruce(final String playerFactionName, final String removedFactionName)
    {
        Validate.notBlank(playerFactionName);
        Validate.notBlank(removedFactionName);

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction removedFaction = getFactionByName(removedFactionName);

        checkNotNull(playerFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), playerFactionName));
        checkNotNull(removedFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), removedFactionName));

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getTruces());
        final Set<String> removedFactionAlliances = new HashSet<>(removedFaction.getTruces());

        playerFactionAlliances.remove(removedFactionName);
        removedFactionAlliances.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setTruces(playerFactionAlliances).build();
        final Faction updatedRemovedFaction = removedFaction.toBuilder().setTruces(removedFactionAlliances).build();

        storageManager.saveFaction(updatedPlayerFaction);
        storageManager.saveFaction(updatedRemovedFaction);
    }

    @Override
    public void addAlly(final String playerFactionName, final String invitedFactionName)
    {
        checkArgument(!StringUtils.isBlank(playerFactionName));
        checkArgument(!StringUtils.isBlank(invitedFactionName));

        if(StringUtils.isBlank(playerFactionName) || StringUtils.isBlank(invitedFactionName))
            throw new IllegalArgumentException("playerFactionName and invitedFactionName must contain a value.");

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction invitedFaction = getFactionByName(invitedFactionName);

        checkNotNull(playerFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), playerFactionName));
        checkNotNull(invitedFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), invitedFactionName));

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getAlliances());
        final Set<String> invitedFactionAlliances = new HashSet<>(invitedFaction.getAlliances());

        playerFactionAlliances.add(invitedFactionName);
        invitedFactionAlliances.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setAlliances(playerFactionAlliances).build();
        final Faction updatedInvitedFaction = invitedFaction.toBuilder().setAlliances(invitedFactionAlliances).build();

        storageManager.saveFaction(updatedPlayerFaction);
        storageManager.saveFaction(updatedInvitedFaction);
    }

    @Override
    public void removeAlly(final String playerFactionName, final String removedFactionName)
    {
        Validate.notBlank(playerFactionName);
        Validate.notBlank(removedFactionName);

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction removedFaction = getFactionByName(removedFactionName);

        checkNotNull(playerFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), playerFactionName));
        checkNotNull(removedFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), removedFactionName));


        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getAlliances());
        final Set<String> removedFactionAlliances = new HashSet<>(removedFaction.getAlliances());

        playerFactionAlliances.remove(removedFactionName);
        removedFactionAlliances.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setAlliances(playerFactionAlliances).build();
        final Faction updatedRemovedFaction = removedFaction.toBuilder().setAlliances(removedFactionAlliances).build();

        storageManager.saveFaction(updatedPlayerFaction);
        storageManager.saveFaction(updatedRemovedFaction);
    }

    @Override
    public void addEnemy(final String playerFactionName, final String enemyFactionName)
    {
        Validate.notBlank(playerFactionName);
        Validate.notBlank(enemyFactionName);

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction enemyFaction = getFactionByName(enemyFactionName);

        checkNotNull(playerFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), playerFactionName));
        checkNotNull(enemyFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), enemyFactionName));

        final Set<String> playerFactionEnemies = new HashSet<>(playerFaction.getEnemies());
        final Set<String> enemyFactionEnemies = new HashSet<>(enemyFaction.getEnemies());

        playerFactionEnemies.add(enemyFactionName);
        enemyFactionEnemies.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setEnemies(playerFactionEnemies).build();
        final Faction updatedEnemyFaction = enemyFaction.toBuilder().setEnemies(enemyFactionEnemies).build();

        storageManager.saveFaction(updatedPlayerFaction);
        storageManager.saveFaction(updatedEnemyFaction);
    }

    @Override
    public void removeEnemy(final String playerFactionName, final String enemyFactionName)
    {
        Validate.notBlank(playerFactionName);
        Validate.notBlank(enemyFactionName);

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction enemyFaction = getFactionByName(enemyFactionName);

        checkNotNull(playerFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), playerFactionName));
        checkNotNull(enemyFaction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), enemyFactionName));

        final Set<String> playerFactionEnemies = new HashSet<>(playerFaction.getEnemies());
        final Set<String> enemyFactionEnemies = new HashSet<>(enemyFaction.getEnemies());

        playerFactionEnemies.remove(enemyFactionName);
        enemyFactionEnemies.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().setEnemies(playerFactionEnemies).build();
        final Faction updatedEnemyFaction = enemyFaction.toBuilder().setEnemies(enemyFactionEnemies).build();

        storageManager.saveFaction(updatedPlayerFaction);
        storageManager.saveFaction(updatedEnemyFaction);
    }

    @Override
    public void addClaims(final Faction faction, final List<Claim> claims)
    {
        checkNotNull(faction);
        checkNotNull(claims);

        final Set<Claim> factionClaims = new HashSet<>(faction.getClaims());

        for(final Claim claim : claims)
        {
            factionClaims.add(claim);
            ParticlesUtil.spawnClaimParticles(claim);
        }

        final Faction updatedFaction = faction.toBuilder().setClaims(factionClaims).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void addClaim(final Faction faction, final Claim claim)
    {
        checkNotNull(faction);
        checkNotNull(claim);

        final Set<Claim> claims = new HashSet<>(faction.getClaims());
        claims.add(claim);
        final Faction updatedFaction = faction.toBuilder().setClaims(claims).build();
        this.storageManager.saveFaction(updatedFaction);

		ParticlesUtil.spawnClaimParticles(claim);
    }

    @Override
    public void removeClaim(final Faction faction, final Claim claim)
    {
        checkNotNull(faction);
        checkNotNull(claim);

        removeClaimInternal(faction, claim);
		ParticlesUtil.spawnUnclaimParticles(claim);
    }

    @Override
    public void destroyClaim(final Faction faction, final Claim claim)
    {
        checkNotNull(faction);
        checkNotNull(claim);

        removeClaimInternal(faction, claim);
        ParticlesUtil.spawnDestroyClaimParticles(claim);
    }

    @Override
    public boolean isClaimed(final UUID worldUUID, final Vector3i chunk)
    {
        checkNotNull(worldUUID);
        checkNotNull(chunk);

        final Optional<Faction> faction = getFactionByChunk(worldUUID, chunk);
        return faction.isPresent();
    }

    @Override
    public boolean isClaimConnected(final Faction faction, final Claim claimToCheck)
    {
        checkNotNull(faction);
        checkNotNull(claimToCheck);

        if (faction.getClaims().size() == 0)
            return true;

        for(final Claim claim : faction.getClaims())
        {
            if(!claimToCheck.getWorldUUID().equals(claim.getWorldUUID()))
                continue;

            final Vector3i chunkToCheck = claimToCheck.getChunkPosition();
            final Vector3i claimChunk = claim.getChunkPosition();

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
    public boolean addClaimOwner(final Faction faction, final Claim claim, final UUID owner)
    {
        checkNotNull(faction);
        checkNotNull(claim);
        checkNotNull(owner);

        final Set<Claim> updatedClaims = new HashSet<>(faction.getClaims());
        updatedClaims.remove(claim);

        final Set<UUID> claimOwners = new HashSet<>(claim.getOwners());
        claimOwners.add(owner);

        final Claim updatedClaim = new Claim(claim.getWorldUUID(), claim.getChunkPosition(), claimOwners, claim.isAccessibleByFaction());
        updatedClaims.add(updatedClaim);

        final Faction updatedFaction = faction.toBuilder().setClaims(updatedClaims).build();

        this.storageManager.saveFaction(updatedFaction);
        return true;
    }

    @Override
    public boolean removeClaimOwner(final Faction faction, final Claim claim, final UUID owner)
    {
        checkNotNull(faction);
        checkNotNull(claim);
        checkNotNull(owner);

        final Set<Claim> updatedClaims = new HashSet<>(faction.getClaims());
        updatedClaims.remove(claim);

        final Set<UUID> claimOwners = new HashSet<>(claim.getOwners());
        claimOwners.remove(owner);

        final Claim updatedClaim = new Claim(claim.getWorldUUID(), claim.getChunkPosition(), claimOwners, claim.isAccessibleByFaction());
        updatedClaims.add(updatedClaim);

        final Faction updatedFaction = faction.toBuilder().setClaims(updatedClaims).build();

        this.storageManager.saveFaction(updatedFaction);
        return true;
    }

    @Override
    public void setClaimAccessibleByFaction(final Faction faction, final Claim claim, final boolean isAccessibleByFaction)
    {
        checkNotNull(faction);
        checkNotNull(claim);

        final Set<Claim> updatedClaims = new HashSet<>(faction.getClaims());
        updatedClaims.remove(claim);

        final Claim updatedClaim = new Claim(claim.getWorldUUID(), claim.getChunkPosition(), claim.getOwners(), isAccessibleByFaction);
        updatedClaims.add(updatedClaim);

        final Faction updatedFaction = faction.toBuilder().setClaims(updatedClaims).build();

        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setHome(Faction faction, @Nullable FactionHome home)
    {
        checkNotNull(faction);

        if (home != null && home.getBlockPosition() != null && home.getWorldUUID() != null)
        {
            faction = faction.toBuilder().setHome(home).build();
        }
        else
        {
            faction = faction.toBuilder().setHome(null).build();
        }

        storageManager.saveFaction(faction);
    }

    @Override
    public List<String> getFactionsTags()
    {
        final Collection<Faction> factionsList = getFactions().values();
        final List<String> factionsTags = new ArrayList<>();

        for(final Faction faction : factionsList)
        {
            factionsTags.add(faction.getTag().toPlain());
        }

        return factionsTags;
    }

    @Override
    public boolean hasOnlinePlayers(final Faction faction)
    {
        checkNotNull(faction);

        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            if(playerManager.isPlayerOnline(faction.getLeader()))
            {
                return true;
            }
        }

        for(final UUID playerUUID : faction.getOfficers())
        {
            if(playerManager.isPlayerOnline(playerUUID))
            {
                return true;
            }
        }

        for(final UUID playerUUID : faction.getMembers())
        {
            if(playerManager.isPlayerOnline(playerUUID))
            {
                return true;
            }
        }

        for(final UUID playerUUID : faction.getRecruits())
        {
            if(playerManager.isPlayerOnline(playerUUID))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void removeAllClaims(final Faction faction)
    {
        checkNotNull(faction);

        for (final Claim claim: faction.getClaims())
        {
            FactionsCache.removeClaim(claim);
//            FactionsCache.updateClaimFaction(claim, Optional.empty());
        }
        final Faction updatedFaction = faction.toBuilder().setClaims(new HashSet<>()).build();
        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void kickPlayer(final UUID playerUUID, final String factionName)
    {
        checkNotNull(playerUUID);
        Validate.notBlank(factionName);

        final Faction faction = getFactionByName(factionName);
        checkNotNull(faction, Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME.replace(Placeholders.FACTION_NAME.getPlaceholder(), factionName));

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

        //Remove player from claim owners
        final Set<Claim> updatedClaims = new HashSet<>();
        for (final Claim claim : faction.getClaims()) {
            final Set<UUID> owners = new HashSet<>(claim.getOwners());
            owners.remove(playerUUID);
            final Claim updatedClaim = new Claim(claim.getWorldUUID(), claim.getChunkPosition(), owners, claim.isAccessibleByFaction());
            updatedClaims.add(updatedClaim);
        }

        final Faction updatedFaction = faction.toBuilder()
                .setOfficers(officers)
                .setMembers(members)
                .setRecruits(recruits)
                .setClaims(updatedClaims)
                .build();
        this.storageManager.saveFaction(updatedFaction);

        //Update player...
        final FactionPlayer factionPlayer = this.storageManager.getPlayer(playerUUID);
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), null, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        this.storageManager.savePlayer(updatedPlayer);
    }

    @Override
    public void startClaiming(final Player player, final Faction faction, final UUID worldUUID, final Vector3i chunkPosition)
    {
        checkNotNull(player);
        checkNotNull(faction);
        checkNotNull(worldUUID);
        checkNotNull(chunkPosition);

        if(this.factionsConfig.shouldDelayClaim())
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.STAY_IN_THE_CHUNK_FOR_NUMBER_SECONDS_TO_CLAIM_IT, TextColors.GREEN, Collections.singletonMap(Placeholders.NUMBER, Text.of(TextColors.GOLD, this.factionsConfig.getClaimDelay())))));
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
            EventRunner.runFactionClaimEventPost(player, faction, player.getWorld(), chunkPosition);
        }
    }

    @Override
    public boolean addClaimByItems(final Player player, final Faction faction, final UUID worldUUID, final Vector3i chunkPosition)
    {
        checkNotNull(player);
        checkNotNull(faction);
        checkNotNull(worldUUID);
        checkNotNull(chunkPosition);

        try
        {
            ItemUtil.pollItemsNeededForClaimFromPlayer(player);
            addClaim(faction, new Claim(worldUUID, chunkPosition));
            return true;
        }
        catch (RequiredItemsNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public void togglePerm(final Faction faction, final FactionMemberType factionMemberType, final FactionPermType factionPermType, final Boolean flagValue)
    {
        checkNotNull(faction);
        checkNotNull(factionMemberType);
        checkNotNull(factionPermType);
        checkNotNull(flagValue);

        final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms = new HashMap<>(faction.getPerms());
        perms.get(factionMemberType).replace(factionPermType, flagValue);

        final Faction updatedFaction = faction.toBuilder().setPerms(perms).build();
        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void changeTagColor(final Faction faction, final TextColor textColor)
    {
        checkNotNull(faction);
        checkNotNull(textColor);

        final Text text = Text.of(textColor, faction.getTag().toPlainSingle());
        final Faction updatedFaction = faction.toBuilder().setTag(text).build();
        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setLastOnline(final Faction faction, final Instant instantTime)
    {
        checkNotNull(faction);
        checkNotNull(instantTime);

        final Faction updatedFaction = faction.toBuilder().setLastOnline(instantTime).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void renameFaction(final Faction faction, final String newFactionName)
    {
        checkNotNull(faction);
        Validate.notBlank(newFactionName);

        this.storageManager.deleteFaction(faction.getName());
        Faction updatedFaction = faction.toBuilder().setName(newFactionName).build();
        this.storageManager.saveFaction(updatedFaction);

        // Update other factions
        CompletableFuture.runAsync(() -> {
            final Set<String> alliances = faction.getAlliances();
            final Set<String> truces = faction.getTruces();
            final Set<String> enemies = faction.getEnemies();
            for (final String alliance : alliances)
            {
                removeAlly(alliance, faction.getName());
                addAlly(alliance, newFactionName);
            }
            for (final String truce : truces)
            {
                removeTruce(truce, faction.getName());
                addTruce(truce, newFactionName);
            }
            for (final String enemy : enemies)
            {
                removeEnemy(enemy, faction.getName());
                addEnemy(enemy, newFactionName);
            }
        });

        //Update players...
        CompletableFuture.runAsync(() -> {
           final Set<UUID> playerUUIDs = updatedFaction.getPlayers();
           for (final UUID playerUUID : playerUUIDs)
           {
               final FactionPlayer factionPlayer = this.storageManager.getPlayer(playerUUID);
               final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), updatedFaction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
               this.storageManager.savePlayer(updatedPlayer);
           }
        });
    }

    @Override
    public void changeTag(final Faction faction, final String newTag)
    {
        checkNotNull(faction);
        Validate.notBlank(newTag);

        final Faction updatedFaction = faction.toBuilder().setTag(Text.of(faction.getTag().getColor(), newTag)).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setChest(final Faction faction, final FactionChest inventory)
    {
        checkNotNull(faction);
        checkNotNull(inventory);

        final Faction updatedFaction = faction.toBuilder().setChest(inventory).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setDescription(final Faction faction, final String description)
    {
        checkNotNull(faction);
        checkNotNull(description);

        final Faction updatedFaction = faction.toBuilder().setDescription(description).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setMessageOfTheDay(final Faction faction, final String motd)
    {
        checkNotNull(faction);
        checkNotNull(motd);

        final Faction updatedFaction = faction.toBuilder().setMessageOfTheDay(motd).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setIsPublic(final Faction faction, final boolean isPublic)
    {
        checkNotNull(faction);

        final Faction updatedFaction = faction.toBuilder().setIsPublic(isPublic).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    private void removeClaimInternal(final Faction faction, final Claim claim)
    {
        final Set<Claim> claims = new HashSet<>(faction.getClaims());
        claims.remove(claim);
        final Faction updatedFaction = faction.toBuilder().setClaims(claims).build();
        FactionsCache.removeClaim(claim);
        this.storageManager.saveFaction(updatedFaction);
//        FactionsCache.updateClaimFaction(claim, Optional.empty());
    }
}
