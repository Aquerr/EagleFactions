package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.api.exception.ActionNotAllowedException;
import io.github.aquerr.eaglefactions.api.exception.PlayerNotInFactionException;
import io.github.aquerr.eaglefactions.api.exception.RankAlreadyExistsException;
import io.github.aquerr.eaglefactions.api.exception.RankLadderPositionOutOfRange;
import io.github.aquerr.eaglefactions.api.exception.RankNotExistsException;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.FactionMemberImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.entities.RankImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class RankManagerImpl implements RankManager
{
    public static final String DEFAULT_RANK_NAME = "default";
    public static final String LEADER_RANK_NAME = "leader";

    private final StorageManager storageManager;

    public RankManagerImpl(final StorageManager storageManager)
    {
        this.storageManager = storageManager;
    }

    public static List<Rank> getDefaultRanks()
    {
        return List.of(
                buildLeaderRank(),
                RankImpl.builder()
                        .name("officer")
                        .displayName("&6Officer")
                        .displayInChat(true)
                        .permissions(Set.of(
                                FactionPermission.INTERACT,
                                FactionPermission.BLOCK_PLACE,
                                FactionPermission.BLOCK_DESTROY,
                                FactionPermission.TERRITORY_CLAIM,
                                FactionPermission.ATTACK,
                                FactionPermission.INVITE_PLAYERS,
                                FactionPermission.KICK_MEMBERS,
                                FactionPermission.VIEW_FACTION_CHEST,
                                FactionPermission.ASSIGN_RANKS,
                                FactionPermission.MANAGE_FACTION_HOME,
                                FactionPermission.MANAGE_MOTD,
                                FactionPermission.MANAGE_DESCRIPTION,
                                FactionPermission.MANAGE_RELATIONS,
                                FactionPermission.MANAGE_INTERNAL_CLAIMS)
                        )
                        .ladderPosition(75)
                        .build(),
                RankImpl.builder()
                        .name("member")
                        .displayName("&6Member")
                        .displayInChat(false)
                        .permissions(Set.of(
                                FactionPermission.INTERACT,
                                FactionPermission.BLOCK_PLACE,
                                FactionPermission.BLOCK_DESTROY,
                                FactionPermission.VIEW_FACTION_CHEST)
                        )
                        .ladderPosition(50)
                        .build(),
                buildDefaultRank()
        );
    }

    public static Rank buildLeaderRank()
    {
        return RankImpl.builder()
                .name(LEADER_RANK_NAME)
                .displayName("&6Leader")
                .displayInChat(true)
                .permissions(Set.of(FactionPermission.values()))
                .ladderPosition(1000)
                .build();
    }

    public static Rank buildDefaultRank()
    {
        return RankImpl.builder()
                .name(DEFAULT_RANK_NAME)
                .displayName("&6Recruit")
                .displayInChat(false)
                .permissions(Set.of(
                        FactionPermission.INTERACT,
                        FactionPermission.BLOCK_PLACE,
                        FactionPermission.BLOCK_DESTROY
                ))
                .ladderPosition(-1000)
                .build();
    }

    @Override
    public boolean setLeader(final @Nullable FactionPlayer targetPlayer, final Faction faction)
    {
        checkNotNull(faction);

        final boolean isCancelled = EventRunner.runFactionLeaderChangeEventPre(null, targetPlayer, faction);
        if (isCancelled)
            return false;

        final Set<FactionMember> members = new HashSet<>(faction.getMembers());

        UUID oldLeaderUUID = faction.getLeader().map(FactionMember::getUniqueId).orElse(null);
        UUID newLeaderUUID = targetPlayer == null ? null : targetPlayer.getUniqueId();

        if (oldLeaderUUID != null)
        {
            Set<String> oldLeaderRanks = members.stream()
                    .filter(member -> member.getUniqueId().equals(oldLeaderUUID))
                    .findFirst()
                    .map(FactionMember::getRankNames)
                    .orElse(Collections.emptySet());
            Set<String> newRanks = new HashSet<>(oldLeaderRanks);
            newRanks.remove(RankManagerImpl.LEADER_RANK_NAME);
            if (newRanks.isEmpty())
                newRanks.add(DEFAULT_RANK_NAME);

            members.removeIf(member -> member.getUniqueId().equals(oldLeaderUUID));
            members.add(new FactionMemberImpl(oldLeaderUUID, newRanks));
        }

        if (newLeaderUUID != null)
        {
            Set<String> newLeaderRanks = members.stream()
                    .filter(member -> member.getUniqueId().equals(newLeaderUUID))
                    .findFirst()
                    .map(FactionMember::getRankNames)
                    .orElse(Collections.emptySet());
            Set<String> newRanks = new HashSet<>(newLeaderRanks);
            newRanks.add(LEADER_RANK_NAME);

            members.removeIf(member -> member.getUniqueId().equals(newLeaderUUID));
            members.add(new FactionMemberImpl(newLeaderUUID, newRanks));
        }

        final Faction updatedFaction = faction.toBuilder()
                .leader(newLeaderUUID)
                .members(members)
                .build();

        storageManager.saveFaction(updatedFaction);
        EventRunner.runFactionLeaderChangeEventPost(null, targetPlayer, faction);
        return true;
    }

    @Override
    public void assignRank(Faction faction, FactionPlayer targetPlayer, Rank rank) throws PlayerNotInFactionException, RankNotExistsException
    {
        // If rank does not exist in faction, throw exception
        if (faction.getRanks().stream().noneMatch(rank1 -> rank1.getName().equalsIgnoreCase(rank.getName())))
            throw new RankNotExistsException();

        if (faction.getMembers().stream().noneMatch(factionMember -> factionMember.getUniqueId().equals(targetPlayer.getUniqueId())))
            throw new PlayerNotInFactionException(targetPlayer);

        updateRanksAndSaveFaction(targetPlayer.getFactionRanks(),
                rank,
                faction,
                targetPlayer,
                (updatedFaction) -> {});
    }

    @Override
    public void createRank(Faction faction, String rankName, int ladderPosition)
            throws RankAlreadyExistsException, RankLadderPositionOutOfRange
    {
        if (faction.getRanks().stream().anyMatch(rank -> rank.getName().equalsIgnoreCase(rankName)))
        {
            throw new RankAlreadyExistsException();
        }

        if (ladderPosition > 1000 || ladderPosition < -1000)
        {
            throw new RankLadderPositionOutOfRange();
        }

        List<Rank> reorderedRanks = insertRankAndReorder(faction.getRanks(), RankImpl.builder()
                .name(rankName)
                .ladderPosition(ladderPosition)
                .displayInChat(true)
                .permissions(Set.of())
                .build());

        final Faction updatedFaction = faction.toBuilder()
                .ranks(reorderedRanks)
                .build();

        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void deleteRank(Faction faction, Rank rank) throws RankNotExistsException, ActionNotAllowedException
    {
        if (rank.getName().equalsIgnoreCase(RankManagerImpl.DEFAULT_RANK_NAME)
                || rank.getName().equalsIgnoreCase(RankManagerImpl.LEADER_RANK_NAME))
        {
            throw new ActionNotAllowedException();
        }

        if (faction.getRanks().stream().noneMatch(factionRank -> factionRank.getName().equalsIgnoreCase(rank.getName())))
        {
            throw new RankNotExistsException();
        }

        // Remove rank from all players
        List<Rank> ranks = new LinkedList<>(faction.getRanks());
        ranks.removeIf(factionRank -> factionRank.isSameRank(rank));

        Set<FactionMember> updatedFactionMembers = new HashSet<>(faction.getMembers());
        for (FactionMember factionMember : faction.getMembers())
        {
            if (factionMember.getRankNames().contains(rank.getName()))
            {
                Set<String> updatedRankNames = new HashSet<>(factionMember.getRankNames());
                updatedRankNames.remove(rank.getName());

                if (updatedRankNames.isEmpty())
                    updatedRankNames.add(RankManagerImpl.DEFAULT_RANK_NAME);

                updatedFactionMembers.removeIf(member -> member.getUniqueId().equals(factionMember.getUniqueId()));

                FactionMember updatedMember = new FactionMemberImpl(factionMember.getUniqueId(), updatedRankNames);
                updatedFactionMembers.add(updatedMember);
            }
        }

        final Faction updatedFaction = faction.toBuilder()
                .ranks(ranks)
                .members(updatedFactionMembers)
                .build();

        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setRankPermission(Faction faction, Rank rank, FactionPermission permission) throws RankNotExistsException
    {
        if (faction.getRanks().stream().noneMatch(factionRank -> factionRank.getName().equalsIgnoreCase(rank.getName())))
        {
            throw new RankNotExistsException();
        }

        Set<FactionPermission> rankPermissions = new HashSet<>(rank.getPermissions());
        if (rankPermissions.contains(permission))
        {
            rankPermissions.remove(permission);
        }
        else
        {
            rankPermissions.add(permission);
        }

        List<Rank> factionRanks = new LinkedList<>(faction.getRanks());
        factionRanks.removeIf(factionRank -> factionRank.isSameRank(rank));
        factionRanks.add(rank.toBuilder()
                .permissions(rankPermissions)
                .build());

        final Faction updatedFaction = faction.toBuilder()
                .ranks(factionRanks)
                .build();

        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setRankDisplayName(Faction faction, Rank rank, String displayName) throws RankNotExistsException
    {
        if (faction.getRanks().stream().noneMatch(factionRank -> factionRank.getName().equalsIgnoreCase(rank.getName())))
        {
            throw new RankNotExistsException();
        }

        List<Rank> factionRanks = new LinkedList<>(faction.getRanks());
        factionRanks.removeIf(factionRank -> factionRank.isSameRank(rank));
        factionRanks.add(rank.toBuilder()
                .displayName(displayName)
                .build());

        final Faction updatedFaction = faction.toBuilder()
                .ranks(factionRanks)
                .build();

        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setRankPosition(Faction faction, Rank rankToUpdate, int ladderPosition) throws RankLadderPositionOutOfRange, RankNotExistsException, ActionNotAllowedException
    {
        if (isDefaultOrLeaderRank(rankToUpdate.getName()))
            throw new ActionNotAllowedException("Default or Leader ranks position cannot be changed!");

        if (faction.getRanks().stream().noneMatch(factionRank -> factionRank.getName().equalsIgnoreCase(rankToUpdate.getName())))
        {
            throw new RankNotExistsException();
        }

        if (ladderPosition > 1000 || ladderPosition < -1000)
        {
            throw new RankLadderPositionOutOfRange();
        }

        List<Rank> reorderedRanks = insertRankAndReorder(faction.getRanks(), rankToUpdate.toBuilder()
                .ladderPosition(ladderPosition)
                .build());

        final Faction updatedFaction = faction.toBuilder()
                .ranks(reorderedRanks)
                .build();

        storageManager.saveFaction(updatedFaction);
    }

    @Override
    public void setRelationPermission(Faction faction, RelationType relationType, FactionPermission permission)
    {

        final Faction.Builder factionBuilder = faction.toBuilder();

        Set<FactionPermission> permissions = new HashSet<>(faction.getRelationPermissions(relationType));
        if (permissions.contains(permission))
            permissions.remove(permission);
        else
            permissions.add(permission);

        if (relationType == RelationType.TRUCE)
        {
            factionBuilder.trucePermissions(permissions);
        }
        else if (relationType == RelationType.ALLIANCE)
        {
            factionBuilder.alliancePermissions(permissions);
        }

        storageManager.saveFaction(factionBuilder.build());
    }

    @Override
    public void setRankDisplayInChat(Faction faction, Rank rank, boolean displayInChat) throws RankNotExistsException
    {
        if (faction.getRanks().stream().noneMatch(factionRank -> factionRank.getName().equalsIgnoreCase(rank.getName())))
        {
            throw new RankNotExistsException();
        }

        List<Rank> factionRanks = new LinkedList<>(faction.getRanks());
        factionRanks.removeIf(factionRank -> factionRank.isSameRank(rank));
        factionRanks.add(rank.toBuilder()
                .displayInChat(displayInChat)
                .build());

        final Faction updatedFaction = faction.toBuilder()
                .ranks(factionRanks)
                .build();

        storageManager.saveFaction(updatedFaction);
    }

    private Rank updateRanksAndSaveFaction(Collection<Rank> oldRanks,
                                           Rank newRank,
                                           Faction faction,
                                           FactionPlayer targetPlayer,
                                           Consumer<Faction> postEvent)
    {
        List<Rank> ranksToSave = new LinkedList<>(oldRanks);
        if (ranksToSave.stream().anyMatch(rank -> rank.isSameRank(newRank)))
        {
            ranksToSave.removeIf(rank -> rank.isSameRank(newRank));
        }
        else
        {
            ranksToSave.add(newRank);
        }

        Set<FactionMember> factionMembers = new HashSet<>(faction.getMembers());
        factionMembers.removeIf(factionMember -> targetPlayer.getUniqueId().equals(factionMember.getUniqueId()));
        factionMembers.add(new FactionMemberImpl(targetPlayer.getUniqueId(), ranksToSave.stream()
                .map(Rank::getName)
                .collect(Collectors.toSet())));

        final Faction updatedFaction = faction.toBuilder()
                .members(factionMembers)
                .build();
        this.storageManager.saveFaction(updatedFaction);

        //Update player
        final FactionPlayer factionPlayer = this.storageManager.getPlayer(targetPlayer.getUniqueId());
        if(factionPlayer != null)
        {
            this.storageManager.savePlayer(new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), updatedFaction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone()));
            postEvent.accept(updatedFaction);
        }

        return newRank;
    }

    public static Rank getHighestRank(Collection<Rank> ranks)
    {
        return ranks.stream()
                .max(Comparator.comparingInt(Rank::getLadderPosition))
                .orElse(null);
    }

    public static Rank getLowestRank(Collection<Rank> ranks)
    {
        return ranks.stream()
                .min(Comparator.comparingInt(Rank::getLadderPosition))
                .orElse(null);
    }

    public static List<Rank> getEditableRanks(Faction faction,
                                              UUID sourcePlayerUUID,
                                              boolean hasAdminMode)
    {
        if (hasAdminMode)
            return faction.getRanks().stream()
                    .filter(rank -> !rank.getName().equalsIgnoreCase(LEADER_RANK_NAME))
                    .collect(Collectors.toList());

        Set<Rank> sourcePlayerRanks = faction.getPlayerRanks(sourcePlayerUUID);

        if (sourcePlayerRanks == null || sourcePlayerRanks.isEmpty())
            return Collections.emptyList();

        int highestRankPosition = sourcePlayerRanks.stream()
                .map(Rank::getLadderPosition)
                .max(Integer::compareTo)
                .orElse(0);

        return faction.getRanks().stream()
                .filter(rank -> rank.getLadderPosition() < highestRankPosition)
                .collect(Collectors.toList());
    }

    private List<Rank> insertRankAndReorder(List<Rank> ranks, Rank newRank)
    {
        List<Rank> sortedRanks = new ArrayList<>(ranks).stream()
                .sorted(Comparator.comparingInt(Rank::getLadderPosition))
                .collect(Collectors.toList());

        int highestRankPos = sortedRanks.stream().map(Rank::getLadderPosition).max(Integer::compareTo).orElse(0);
        int lowestRankPos = sortedRanks.stream().map(Rank::getLadderPosition).min(Integer::compareTo).orElse(0);
        int newRankLadderPos = newRank.getLadderPosition();

        sortedRanks.removeIf(factionRank -> factionRank.isSameRank(newRank));

        List<Rank> resultList = new ArrayList<>(sortedRanks.size() + 1);

        if (newRankLadderPos > highestRankPos)
        {
            resultList.addAll(sortedRanks);
            resultList.add(newRank.toBuilder()
                    .build());
            return resultList;
        }
        else if (newRankLadderPos < lowestRankPos)
        {
            resultList.add(newRank.toBuilder()
                    .build());
            resultList.addAll(sortedRanks);
            return resultList;
        }

        Rank rankToInsert = newRank;
        // Reorder ranks
        int ranksCount = sortedRanks.size();
        for (int i = 0; i < ranksCount; i++)
        {
            Rank rank = sortedRanks.get(i);
            if (rankToInsert == null || rank.getLadderPosition() != rankToInsert.getLadderPosition())
            {
                resultList.add(rank);
                continue;
            }

            resultList.add(rankToInsert);
            rankToInsert = rank.toBuilder()
                    .ladderPosition(rank.getLadderPosition() + 1)
                    .build();
        }
        resultList.add(rankToInsert);

        return resultList;
    }

    private boolean isDefaultOrLeaderRank(String rankName)
    {
        return DEFAULT_RANK_NAME.equalsIgnoreCase(rankName) || LEADER_RANK_NAME.equalsIgnoreCase(rankName);
    }
}
