package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
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
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final StorageManager storageManager;

    public RankManagerImpl(final StorageManager storageManager)
    {
        this.storageManager = storageManager;
    }

    public static List<Rank> getDefaultRanks()
    {
        return List.of(
                RankImpl.builder()
                        .name("leader")
                        .displayName("&6Leader")
                        .permissions(Set.of(FactionPermission.values()))
                        .ladderPosition(4)
                        .build(),
                RankImpl.builder()
                        .name("officer")
                        .displayName("&6Officer")
                        .permissions(Set.of(
                                FactionPermission.INTERACT,
                                FactionPermission.BLOCK_PLACE,
                                FactionPermission.BLOCK_DESTROY,
                                FactionPermission.TERRITORY_CLAIM,
                                FactionPermission.ATTACK,
                                FactionPermission.INVITE_PLAYERS,
                                FactionPermission.VIEW_FACTION_CHEST,
                                FactionPermission.INTERNAL_CLAIM_BYPASS_ACCESS,
                                FactionPermission.KICK_MEMBERS,
                                FactionPermission.MANAGE_RANKS,
                                FactionPermission.MANAGE_RELATIONS,
                                FactionPermission.MANAGE_DESCRIPTION,
                                FactionPermission.MANAGE_FACTION_HOME,
                                FactionPermission.MANAGE_IS_PUBLIC,
                                FactionPermission.MANAGE_INTERNAL_CLAIMS,
                                FactionPermission.MANAGE_MOTD,
                                FactionPermission.MANAGE_TAG_COLOR,
                                FactionPermission.MANAGE_TAG_NAME)
                        )
                        .ladderPosition(3)
                        .build(),
                RankImpl.builder()
                        .name("member")
                        .displayName("&6Member")
                        .permissions(Set.of(
                                FactionPermission.INTERACT,
                                FactionPermission.BLOCK_PLACE,
                                FactionPermission.BLOCK_DESTROY,
                                FactionPermission.VIEW_FACTION_CHEST)
                        )
                        .ladderPosition(2)
                        .build(),
                buildDefaultRecruitRank()
        );
    }

    public static Rank buildDefaultRecruitRank()
    {
        return RankImpl.builder()
                .name("recruit")
                .displayName("&6Recruit")
                .permissions(Set.of(
                        FactionPermission.INTERACT,
                        FactionPermission.BLOCK_PLACE,
                        FactionPermission.BLOCK_DESTROY
                ))
                .ladderPosition(1)
                .build();
    }

//    @Override
//    public Rank demotePlayer(final @Nullable ServerPlayer demotedBy,
//                             final FactionPlayer targetPlayer) throws PlayerNotInFactionException
//    {
//        checkNotNull(targetPlayer);
//
//        Faction faction = targetPlayer.getFaction()
//                .orElseThrow(() -> new PlayerNotInFactionException(targetPlayer));
//
//        final boolean isCancelled = EventRunner.runFactionDemoteEventPre(demotedBy, targetPlayer, faction);
//
//        Rank currentRank = targetPlayer.getFactionRank().get();
//        if (isCancelled)
//            return currentRank;
//
//        Rank demotedTo = faction.getRanks().stream().sorted(Comparator.comparingInt(Rank::getLadderPosition).reversed())
//                .filter(rank -> rank.getLadderPosition() < currentRank.getLadderPosition())
//                .findFirst()
//                .orElse(currentRank);
//
//        return updateRanksAndSaveFaction(currentRank, demotedTo, faction, targetPlayer, (updatedFaction) -> EventRunner.runFactionDemoteEventPost(demotedBy, targetPlayer, demotedTo, updatedFaction));
//    }

//    @Override
//    public Rank promotePlayer(final @Nullable ServerPlayer promotedBy,
//                              final FactionPlayer targetPlayer) throws PlayerNotInFactionException
//    {
//        checkNotNull(targetPlayer);
//
//        Faction faction = targetPlayer.getFaction()
//                .orElseThrow(() -> new PlayerNotInFactionException(targetPlayer));
//
//        final boolean isCancelled = EventRunner.runFactionPromoteEventPre(promotedBy, targetPlayer, faction);
//
//        Rank currentRank = targetPlayer.getFactionRanks();
//        if (isCancelled)
//            return currentRank;
//
//        int highestRankPosition = getHighestRank(targetPlayer.getFactionRanks()).getLadderPosition();
//
//        Rank promotedTo = faction.getRanks().stream().sorted(Comparator.comparingInt(Rank::getLadderPosition))
//                .filter(rank -> rank.getLadderPosition() > currentRank.getLadderPosition())
//                .findFirst()
//                .orElse(currentRank);
//
//        return updateRanksAndSaveFaction(currentRank, promotedTo, faction, targetPlayer, (updatedFaction) -> EventRunner.runFactionPromoteEventPost(promotedBy, targetPlayer, promotedTo, updatedFaction));
//    }

    @Override
    public boolean setLeader(final @Nullable FactionPlayer targetPlayer, final Faction faction)
    {
        checkNotNull(faction);

        final boolean isCancelled = EventRunner.runFactionLeaderChangeEventPre(null, targetPlayer, faction);
        if (isCancelled)
            return false;

        final Set<FactionMember> members = new HashSet<>(faction.getMembers());

        UUID newLeaderUUID;

        if (targetPlayer == null)
        {
            newLeaderUUID = new UUID(0, 0);
        }
        else
        {
            newLeaderUUID = targetPlayer.getUniqueId();
        }

//        try
//        {
//            demotePlayer(null, targetPlayer);
//        }
//        catch (PlayerNotInFactionException e)
//        {
//            throw new RuntimeException(e);
//        }

        final Faction updatedFaction = faction.toBuilder()
                .leader(newLeaderUUID)
                .members(members)
                .build();

        storageManager.saveFaction(updatedFaction);
        EventRunner.runFactionLeaderChangeEventPost(null, targetPlayer, faction);
        return true;
    }

    @Override
    public void assignRank(ServerPlayer player, Faction faction, FactionPlayer targetPlayer, Rank rank) throws PlayerNotInFactionException, RankNotExistsException
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

        if (ladderPosition > 100 || ladderPosition < -100)
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
    public void deleteRank(Faction faction, Rank rank) throws RankNotExistsException
    {
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
                Set<String> updatedRankNames = factionMember.getRankNames();
                updatedRankNames.remove(rank.getName());

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
    public void setRankPosition(Faction faction, Rank rankToUpdate, int ladderPosition) throws RankLadderPositionOutOfRange, RankNotExistsException
    {
        if (faction.getRanks().stream().noneMatch(factionRank -> factionRank.getName().equalsIgnoreCase(rankToUpdate.getName())))
        {
            throw new RankNotExistsException();
        }

        if (ladderPosition > 100 || ladderPosition < -100)
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
    public void setDefaultRank(Faction faction, Rank rank) throws RankNotExistsException
    {
        if (faction.getRanks().stream().noneMatch(factionRank -> factionRank.getName().equalsIgnoreCase(rank.getName())))
        {
            throw new RankNotExistsException();
        }

        final Faction updatedFaction = faction.toBuilder()
                .defaultRankName(rank.getName())
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

    private Rank updateRanksAndSaveFaction(List<Rank> oldRanks,
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

    public static Rank getHighestRank(List<Rank> ranks)
    {
        return ranks.stream()
                .max(Comparator.comparingInt(Rank::getLadderPosition))
                .orElse(null);
    }

    public static Rank getLowestRank(List<Rank> ranks)
    {
        return ranks.stream()
                .min(Comparator.comparingInt(Rank::getLadderPosition))
                .orElse(null);
    }

    public static List<Rank> getEditableRanks(Faction faction,
                                              UUID promoterUUID,
                                              boolean hasAdminMode)
    {
        if (hasAdminMode)
            return faction.getRanks();

        List<Rank> promoterRanks = faction.getPlayerRanks(promoterUUID);

        if (promoterRanks == null || promoterRanks.isEmpty())
            return Collections.emptyList();

        int highestRankPosition = promoterRanks.stream()
                .map(Rank::getLadderPosition)
                .max(Integer::compareTo)
                .orElse(0);

        if (promoterUUID.equals(faction.getLeader().getUniqueId()))
            return faction.getRanks().stream()
                    .filter(rank -> rank.getLadderPosition() < highestRankPosition)
                    .collect(Collectors.toList());

        return faction.getRanks().stream()
                .filter(rank -> rank.getLadderPosition() < highestRankPosition - 1)
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
}
