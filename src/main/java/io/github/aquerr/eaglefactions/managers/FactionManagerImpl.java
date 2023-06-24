package io.github.aquerr.eaglefactions.managers;

import com.google.common.base.Preconditions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPermType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlags;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.api.logic.FactionManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.claim.provider.FactionMaxClaimCountProvider;
import io.github.aquerr.eaglefactions.api.math.Vector3i;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.api.text.Text;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.model.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.model.ProtectionFlagImpl;
import io.github.aquerr.eaglefactions.model.ProtectionFlagsImpl;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class FactionManagerImpl implements FactionManager
{
    private static final UUID DUMMY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final Set<FactionMaxClaimCountProvider> factionMaxClaimCountProviders = new HashSet<>();

    private final StorageManager storageManager;
    private final FactionsConfig factionsConfig;
    private final PlayerManager playerManager;
    private final MessageService messageService;

    public FactionManagerImpl(final PlayerManager playerManager, final StorageManager storageManager, final FactionsConfig factionsConfig, final MessageService messageService)
    {
        this.storageManager = storageManager;
        this.playerManager = playerManager;
        this.factionsConfig = factionsConfig;
        this.messageService = messageService;
    }

    @Override
    public void addFactionMaxClaimCountProvider(FactionMaxClaimCountProvider provider)
    {
        this.factionMaxClaimCountProviders.add(provider);
    }

    @Override
    public void setFactionMaxClaimCountProviders(Set<FactionMaxClaimCountProvider> providers)
    {
        this.factionMaxClaimCountProviders.clear();
        this.factionMaxClaimCountProviders.addAll(providers);
    }

    @Override
    public Set<FactionMaxClaimCountProvider> getFactionMaxClaimCountProviders()
    {
        return Collections.unmodifiableSet(this.factionMaxClaimCountProviders);
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
    public List<ServerPlayer> getOnlinePlayers(final Faction faction)
    {
        checkNotNull(faction);

        final List<ServerPlayer> factionPlayers = new ArrayList<>();
        final UUID factionLeader = faction.getLeader();
        if(!faction.getLeader().equals(DUMMY_UUID) && this.playerManager.isPlayerOnline(factionLeader))
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

        Preconditions.checkNotNull(factionToDisband, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", factionName));

        //Update players...
        CompletableFuture.runAsync(() -> {
            final Set<UUID> playerUUIDs = factionToDisband.getPlayers();
            for (final UUID playerUUID : playerUUIDs)
            {
                //Faction Player should always exist, so we do not need to check if it is present.
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
                })
                .thenRunAsync(() -> this.storageManager.deleteFaction(factionName));
        return true;
    }

    @Override
    public void leaveFaction(final UUID playerUUID, final String factionName)
    {
        checkNotNull(playerUUID);
        checkNotNull(factionName);

        final Faction faction = getFactionByName(factionName);

        checkNotNull(faction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", factionName));

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
                                .setLeader(DUMMY_UUID)
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
                if (!DUMMY_UUID.equals(leaderUUID))
                {
                    officers.add(leaderUUID);
                }
                factionBuilder.setLeader(playerUUID).setOfficers(officers);
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

        //Save player...
        final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(playerUUID).get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), faction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        this.storageManager.savePlayer(updatedPlayer);
    }

    @Override
    public void addTruce(final String playerFactionName, final String invitedFactionName)
    {
        Validate.notBlank(playerFactionName);
        Validate.notBlank(invitedFactionName);

        final Faction playerFaction = getFactionByName(playerFactionName);
        final Faction invitedFaction = getFactionByName(invitedFactionName);

        checkNotNull(playerFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", playerFactionName));
        checkNotNull(invitedFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", invitedFactionName));

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

        checkNotNull(playerFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", playerFactionName));
        checkNotNull(removedFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", removedFactionName));

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

        checkNotNull(playerFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", playerFactionName));
        checkNotNull(invitedFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", invitedFactionName));

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

        checkNotNull(playerFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", playerFactionName));
        checkNotNull(removedFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", removedFactionName));


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

        checkNotNull(playerFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", playerFactionName));
        checkNotNull(enemyFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", enemyFactionName));

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

        checkNotNull(playerFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", playerFactionName));
        checkNotNull(enemyFaction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", enemyFactionName));

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
    public List<Text> getFactionsTags()
    {
        final Collection<Faction> factionsList = getFactions().values();
        final List<Text> factionsTags = new ArrayList<>();

        for(final Faction faction : factionsList)
        {
            factionsTags.add(faction.getTag());
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
        checkNotNull(faction, messageService.resolveMessage("error.general.there-is-no-faction-called-faction-name", factionName));

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
    public void startClaiming(final ServerPlayer player, final Faction faction, final UUID worldUUID, final Vector3i chunkPosition)
    {
        checkNotNull(player);
        checkNotNull(faction);
        checkNotNull(worldUUID);
        checkNotNull(chunkPosition);

        if(this.factionsConfig.shouldDelayClaim())
        {
            player.sendSystemMessage(messageService.resolveMessageWithPrefix("command.claim.stay-in-the-chunk-for-number-of-seconds-to-claim-it", this.factionsConfig.getClaimDelay()));
//            EagleFactionsScheduler.getInstance().scheduleWithDelayedInterval(new ClaimDelayTask(player, chunkPosition), 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
        }
        else
        {
            if(this.factionsConfig.shouldClaimByItems())
            {
                boolean didSucceed = addClaimByItems(player, faction, worldUUID, chunkPosition);
                if(didSucceed)
                    player.sendSystemMessage(messageService.resolveMessageWithPrefix("command.claim.land-has-been-successfully-claimed", chunkPosition.toString()));
                else
                    player.sendSystemMessage(Component.empty().append(PluginInfo.ERROR_PREFIX).append(messageService.resolveComponentWithMessage("error.command.claim.not-enough-resources")));
            }
            else
            {
                player.sendSystemMessage(messageService.resolveMessageWithPrefix("command.claim.land-has-been-successfully-claimed", chunkPosition.toString()));
                addClaim(faction, new Claim(worldUUID, chunkPosition));
            }
            EventRunner.runFactionClaimEventPost(player, faction, player.getLevel(), chunkPosition);
        }
    }

    @Override
    public boolean addClaimByItems(final ServerPlayer player, final Faction faction, final UUID worldUUID, final Vector3i chunkPosition)
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
            player.sendSystemMessage(Component.empty().append(PluginInfo.ERROR_PREFIX).append(messageService.resolveComponentWithMessage("error.command.claim.not-enough-resources", e.buildAllRequiredItemsMessage())));
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
    public void changeTagColor(final Faction faction, final ChatFormatting textColor)
    {
        checkNotNull(faction);
        checkNotNull(textColor);

        final Text text = new Text(faction.getTag().getText(), textColor.getName());
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
        }).thenRunAsync(() -> {
            final Set<UUID> playerUUIDs = updatedFaction.getPlayers();
            for (final UUID playerUUID : playerUUIDs)
            {
                final FactionPlayer factionPlayer = this.storageManager.getPlayer(playerUUID);
                final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), updatedFaction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
                this.storageManager.savePlayer(updatedPlayer);
            }
        }).thenRunAsync(() -> this.storageManager.deleteFaction(faction.getName()));
    }

    @Override
    public void changeTag(final Faction faction, final String newTag)
    {
        checkNotNull(faction);
        Validate.notBlank(newTag);

        final Faction updatedFaction = faction.toBuilder().setTag(new Text(newTag, faction.getTag().getColor())).build();
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

    @Override
    public int getFactionMaxClaims(final Faction faction)
    {
        int maxclaims = 0;
        for (final FactionMaxClaimCountProvider provider : this.factionMaxClaimCountProviders)
        {
            maxclaims = maxclaims + provider.getMaxClaimCount(faction);
        }
        return maxclaims;
    }

    @Override
    public void setFactionProtectionFlag(Faction faction, ProtectionFlagType flagType, boolean value)
    {
        checkNotNull(faction);
        checkNotNull(flagType);

        ProtectionFlags protectionFlags = new ProtectionFlagsImpl(faction.getProtectionFlags());
        protectionFlags.putFlag(new ProtectionFlagImpl(flagType, value));
        Faction updatedFaction = faction.toBuilder().setProtectionFlags(protectionFlags.getProtectionFlags()).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    private void removeClaimInternal(final Faction faction, final Claim claim)
    {
        final Set<Claim> claims = new HashSet<>(faction.getClaims());
        claims.remove(claim);
        final Faction updatedFaction = faction.toBuilder().setClaims(claims).build();
        FactionsCache.removeClaim(claim);
        this.storageManager.saveFaction(updatedFaction);
    }
}
