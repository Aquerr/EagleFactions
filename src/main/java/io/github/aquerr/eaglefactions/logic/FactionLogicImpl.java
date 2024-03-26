package io.github.aquerr.eaglefactions.logic;

import com.google.common.base.Preconditions;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlags;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimContext;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimStrategy;
import io.github.aquerr.eaglefactions.api.managers.claim.NoCostClaimStrategy;
import io.github.aquerr.eaglefactions.api.managers.claim.provider.FactionMaxClaimCountProvider;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.FactionMemberImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagImpl;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagsImpl;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.managers.claim.ClaimStrategyManager;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.math.vector.Vector3i;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.kyori.adventure.text.Component.text;

public class FactionLogicImpl implements FactionLogic
{
    private static final String THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY = "error.general.there-is-no-faction-called-faction-name";

    private final Set<FactionMaxClaimCountProvider> factionMaxClaimCountProviders = new HashSet<>();
    private ClaimStrategy claimStrategy = new NoCostClaimStrategy(this);
    private final ClaimStrategyManager claimStrategyManager;

    private final StorageManager storageManager;
    private final PlayerManager playerManager;
    private final MessageService messageService;


    public FactionLogicImpl(final PlayerManager playerManager,
                            final StorageManager storageManager,
                            final MessageService messageService,
                            final ClaimStrategyManager claimStrategyManager)
    {
        this.storageManager = storageManager;
        this.playerManager = playerManager;
        this.messageService = messageService;
        this.claimStrategyManager = claimStrategyManager;
    }

    @Override
    public void setClaimStrategy(ClaimStrategy claimStrategy)
    {
        this.claimStrategy = claimStrategy;
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
        if (cachedOptional.isPresent())
            return cachedOptional;

        for(Faction faction : getFactions().values())
        {
            if(faction.getClaims().contains(claim))
            {
                FactionsCache.updateClaimFaction(claim, faction);
                return Optional.of(faction);
            }
        }

        FactionsCache.removeClaim(claim);
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
        final UUID leaderUUID = faction.getLeader()
                .map(FactionMember::getUniqueId)
                .orElse(null);
        if(leaderUUID != null && this.playerManager.isPlayerOnline(leaderUUID))
        {
            factionPlayers.add(playerManager.getPlayer(leaderUUID).get());
        }

        for(final FactionMember factionMember : faction.getMembers())
        {
            if(playerManager.isPlayerOnline(factionMember.getUniqueId()))
            {
                factionPlayers.add(playerManager.getPlayer(factionMember.getUniqueId()).get());
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
    public Map<Claim, Faction> getAllClaims()
    {
        return FactionsCache.getClaims();
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

        Preconditions.checkNotNull(factionToDisband, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, factionName));

        //Update players...
        CompletableFuture.runAsync(() -> {
            final Set<FactionMember> playerUUIDs = factionToDisband.getMembers();
            for (final FactionMember playerUUID : playerUUIDs)
            {
                //Faction Player should always exist, so we do not need to check if it is present.
                final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(playerUUID.getUniqueId()).get();
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

        checkNotNull(faction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, factionName));

        if (playerUUID.equals(faction.getLeader().map(FactionMember::getUniqueId).orElse(null)))
            throw new IllegalArgumentException(messageService.resolveMessage("error.command.leave.you-cant-leave-your-faction-because-you-are-its-leader"));

        final Set<FactionMember> members = new HashSet<>(faction.getMembers());

        members.removeIf(member -> member.getUniqueId().equals(playerUUID));

        //Remove player from claim owners
        final Set<Claim> updatedClaims = new HashSet<>();
        for (final Claim claim : faction.getClaims()) {
            final Set<UUID> owners = new HashSet<>(claim.getOwners());
            owners.remove(playerUUID);
            final Claim updatedClaim = new Claim(claim.getWorldUUID(), claim.getChunkPosition(), owners, claim.isAccessibleByFaction());
            updatedClaims.add(updatedClaim);
        }

        final Faction updatedFaction = faction.toBuilder().members(members).claims(updatedClaims).build();
        storageManager.saveFaction(updatedFaction);

        //Save player...
        final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(playerUUID).get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), null, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        this.storageManager.savePlayer(updatedPlayer);
    }

    @Override
    public void setFaction(UUID playerUUID,
                           String factionName,
                           @Nullable String rankName)
    {
        checkNotNull(playerUUID);
        checkNotNull(factionName);

        // Delete player in old faction
        getFactionByPlayerUUID(playerUUID)
                .ifPresent((faction) -> {
                    if (playerUUID.equals(faction.getLeader()
                            .map(FactionMember::getUniqueId)
                            .orElse(null)))
                    {
                        EagleFactionsPlugin.getPlugin().getRankManager().setLeader(
                                null, faction
                        );
                    }
                    leaveFaction(playerUUID, faction.getName());
                });

        Faction faction = getFactionByName(factionName);
        checkNotNull(faction);

        final Set<FactionMember> members = new HashSet<>(faction.getMembers());
        Rank newRank = Optional.ofNullable(rankName)
                .flatMap(r -> faction.getRanks().stream()
                        .filter(rank -> rank.getName().equals(rankName))
                        .findFirst()
                )
                .orElse(faction.getDefaultRank());

        members.add(new FactionMemberImpl(playerUUID, Set.of(newRank.getName())));

        Faction.Builder factionBuilder = faction.toBuilder();
        factionBuilder.members(members);
        if (newRank.getName().equalsIgnoreCase(RankManagerImpl.LEADER_RANK_NAME))
            factionBuilder.leader(playerUUID);

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

        checkNotNull(playerFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, playerFactionName));
        checkNotNull(invitedFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, invitedFactionName));

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getTruces());
        final Set<String> invitedFactionAlliances = new HashSet<>(invitedFaction.getTruces());

        playerFactionAlliances.add(invitedFactionName);
        invitedFactionAlliances.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().truces(playerFactionAlliances).build();
        final Faction updatedInvitedFaction = invitedFaction.toBuilder().truces(invitedFactionAlliances).build();

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

        checkNotNull(playerFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, playerFactionName));
        checkNotNull(removedFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, removedFactionName));

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getTruces());
        final Set<String> removedFactionAlliances = new HashSet<>(removedFaction.getTruces());

        playerFactionAlliances.remove(removedFactionName);
        removedFactionAlliances.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().truces(playerFactionAlliances).build();
        final Faction updatedRemovedFaction = removedFaction.toBuilder().truces(removedFactionAlliances).build();

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

        checkNotNull(playerFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, playerFactionName));
        checkNotNull(invitedFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, invitedFactionName));

        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getAlliances());
        final Set<String> invitedFactionAlliances = new HashSet<>(invitedFaction.getAlliances());

        playerFactionAlliances.add(invitedFactionName);
        invitedFactionAlliances.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().alliances(playerFactionAlliances).build();
        final Faction updatedInvitedFaction = invitedFaction.toBuilder().alliances(invitedFactionAlliances).build();

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

        checkNotNull(playerFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, playerFactionName));
        checkNotNull(removedFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, removedFactionName));


        final Set<String> playerFactionAlliances = new HashSet<>(playerFaction.getAlliances());
        final Set<String> removedFactionAlliances = new HashSet<>(removedFaction.getAlliances());

        playerFactionAlliances.remove(removedFactionName);
        removedFactionAlliances.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().alliances(playerFactionAlliances).build();
        final Faction updatedRemovedFaction = removedFaction.toBuilder().alliances(removedFactionAlliances).build();

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

        checkNotNull(playerFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, playerFactionName));
        checkNotNull(enemyFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, enemyFactionName));

        final Set<String> playerFactionEnemies = new HashSet<>(playerFaction.getEnemies());
        final Set<String> enemyFactionEnemies = new HashSet<>(enemyFaction.getEnemies());

        playerFactionEnemies.add(enemyFactionName);
        enemyFactionEnemies.add(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().enemies(playerFactionEnemies).build();
        final Faction updatedEnemyFaction = enemyFaction.toBuilder().enemies(enemyFactionEnemies).build();

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

        checkNotNull(playerFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, playerFactionName));
        checkNotNull(enemyFaction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, enemyFactionName));

        final Set<String> playerFactionEnemies = new HashSet<>(playerFaction.getEnemies());
        final Set<String> enemyFactionEnemies = new HashSet<>(enemyFaction.getEnemies());

        playerFactionEnemies.remove(enemyFactionName);
        enemyFactionEnemies.remove(playerFactionName);

        final Faction updatedPlayerFaction = playerFaction.toBuilder().enemies(playerFactionEnemies).build();
        final Faction updatedEnemyFaction = enemyFaction.toBuilder().enemies(enemyFactionEnemies).build();

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

        final Faction updatedFaction = faction.toBuilder().claims(factionClaims).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void addClaim(final Faction faction, final Claim claim)
    {
        checkNotNull(faction);
        checkNotNull(claim);

        final Faction factionToUpdate = getFactionByName(faction.getName());
        checkNotNull(factionToUpdate);

        final Set<Claim> claims = new HashSet<>(factionToUpdate.getClaims());
        claims.add(claim);
        final Faction updatedFaction = factionToUpdate.toBuilder().claims(claims).build();
        this.storageManager.saveFaction(updatedFaction);

		ParticlesUtil.spawnClaimParticles(claim);
    }

    @Override
    public void removeClaim(final Faction faction, final Claim claim)
    {
        checkNotNull(faction);
        checkNotNull(claim);

        removeClaimInternal(faction.getName(), claim);
		ParticlesUtil.spawnUnclaimParticles(claim);
    }

    @Override
    public void destroyClaim(final Faction faction, final Claim claim)
    {
        checkNotNull(faction);
        checkNotNull(claim);

        removeClaimInternal(faction.getName(), claim);
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

        if (faction.getClaims().isEmpty())
            return true;

        for(final Claim claim : faction.getClaims())
        {
            if(!claimToCheck.getWorldUUID().equals(claim.getWorldUUID()))
                continue;

            final Vector3i chunkToCheck = claimToCheck.getChunkPosition();
            final Vector3i claimChunk = claim.getChunkPosition();

            if((claimChunk.x() == chunkToCheck.x()) && ((claimChunk.z() + 1 == chunkToCheck.z()) || (claimChunk.z() - 1 == chunkToCheck.z())))
            {
                return true;
            }
            else if((claimChunk.z() == chunkToCheck.z()) && ((claimChunk.x() + 1 == chunkToCheck.x()) || (claimChunk.x() - 1 == chunkToCheck.x())))
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

        final Faction updatedFaction = faction.toBuilder().claims(updatedClaims).build();

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

        final Faction updatedFaction = faction.toBuilder().claims(updatedClaims).build();

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

        final Faction updatedFaction = faction.toBuilder().claims(updatedClaims).build();

        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setHome(Faction faction, @Nullable FactionHome home)
    {
        checkNotNull(faction);

        if (home != null && home.getBlockPosition() != null && home.getWorldUUID() != null)
        {
            faction = faction.toBuilder().home(home).build();
        }
        else
        {
            faction = faction.toBuilder().home(null).build();
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
            factionsTags.add(PlainTextComponentSerializer.plainText().serialize(faction.getTag()));
        }

        return factionsTags;
    }

    @Override
    public boolean hasOnlinePlayers(final Faction faction)
    {
        checkNotNull(faction);

        UUID leaderUUID = faction.getLeader()
                .map(FactionMember::getUniqueId)
                .orElse(null);
        if(leaderUUID != null && (playerManager.isPlayerOnline(leaderUUID)))
        {
                return true;
        }

        for(final FactionMember factionMember : faction.getMembers())
        {
            if(playerManager.isPlayerOnline(factionMember.getUniqueId()))
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
        }
        final Faction updatedFaction = faction.toBuilder().claims(new HashSet<>()).build();
        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void kickPlayer(final UUID playerUUID, final String factionName)
    {
        checkNotNull(playerUUID);
        Validate.notBlank(factionName);

        final Faction faction = getFactionByName(factionName);
        checkNotNull(faction, messageService.resolveMessage(THERE_IS_NOT_FACTION_CALLED_FACTION_NAME_MESSAGE_KEY, factionName));

        final Set<FactionMember> members = new HashSet<>(faction.getMembers());
        members.removeIf(member -> member.getUniqueId().equals(playerUUID));

        //Remove player from claim owners
        final Set<Claim> updatedClaims = new HashSet<>();
        for (final Claim claim : faction.getClaims()) {
            final Set<UUID> owners = new HashSet<>(claim.getOwners());
            owners.remove(playerUUID);
            final Claim updatedClaim = new Claim(claim.getWorldUUID(), claim.getChunkPosition(), owners, claim.isAccessibleByFaction());
            updatedClaims.add(updatedClaim);
        }

        final Faction updatedFaction = faction.toBuilder()
                .members(members)
                .claims(updatedClaims)
                .build();
        this.storageManager.saveFaction(updatedFaction);

        //Update player...
        final FactionPlayer factionPlayer = this.storageManager.getPlayer(playerUUID);
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), null, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        this.storageManager.savePlayer(updatedPlayer);
    }

    @Override
    public void startClaiming(ClaimContext claimContext)
    {
        checkNotNull(claimContext.getFaction());
        checkNotNull(claimContext.getServerPlayer());
        checkNotNull(claimContext.getServerLocation());

        claimStrategyManager.claim(claimStrategy, claimContext);
    }

    @Override
    public void changeTagColor(final Faction faction, final NamedTextColor textColor)
    {
        checkNotNull(faction);
        checkNotNull(textColor);

        final TextComponent text = text(PlainTextComponentSerializer.plainText().serialize(faction.getTag()), textColor);
        final Faction updatedFaction = faction.toBuilder().tag(text).build();
        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setLastOnline(final Faction faction, final Instant instantTime)
    {
        checkNotNull(faction);
        checkNotNull(instantTime);

        final Faction updatedFaction = faction.toBuilder().lastOnline(instantTime).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void renameFaction(final Faction faction, final String newFactionName)
    {
        checkNotNull(faction);
        Validate.notBlank(newFactionName);

        Faction updatedFaction = faction.toBuilder().name(newFactionName).build();
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
           final Set<FactionMember> playerUUIDs = updatedFaction.getMembers();
           for (final FactionMember playerUUID : playerUUIDs)
           {
               final FactionPlayer factionPlayer = this.storageManager.getPlayer(playerUUID.getUniqueId());
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

        final Faction updatedFaction = faction.toBuilder().tag(text(newTag, faction.getTag().color())).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setChest(final Faction faction, final FactionChest inventory)
    {
        checkNotNull(faction);
        checkNotNull(inventory);

        final Faction updatedFaction = faction.toBuilder().chest(inventory).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setDescription(final Faction faction, final String description)
    {
        checkNotNull(faction);
        checkNotNull(description);

        final Faction updatedFaction = faction.toBuilder().description(description).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setMessageOfTheDay(final Faction faction, final String motd)
    {
        checkNotNull(faction);
        checkNotNull(motd);

        final Faction updatedFaction = faction.toBuilder().messageOfTheDay(motd).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setIsPublic(final Faction faction, final boolean isPublic)
    {
        checkNotNull(faction);

        final Faction updatedFaction = faction.toBuilder().isPublic(isPublic).build();
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
        Faction updatedFaction = faction.toBuilder().protectionFlags(protectionFlags.getProtectionFlags()).build();
        this.storageManager.saveFaction(updatedFaction);
    }

    private void removeClaimInternal(final String factionName, final Claim claim)
    {
        final Faction faction = getFactionByName(factionName);
        final Set<Claim> claims = new HashSet<>(faction.getClaims());
        claims.remove(claim);
        final Faction updatedFaction = faction.toBuilder().claims(claims).build();
        FactionsCache.removeClaim(claim);
        this.storageManager.saveFaction(updatedFaction);
    }
}
