package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.exception.PlayerNotInFactionException;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class RankManagerImpl implements RankManager
{
    private final FactionLogic factionLogic;
    private final StorageManager storageManager;

    public RankManagerImpl(FactionLogic factionLogic, final StorageManager storageManager)
    {
        this.factionLogic = factionLogic;
        this.storageManager = storageManager;
    }

    @Override
    public FactionMemberType demotePlayer(final @Nullable ServerPlayer player, final FactionPlayer targetPlayer) throws PlayerNotInFactionException
    {
        checkNotNull(targetPlayer);

        Faction faction = targetPlayer.getFaction()
                .orElseThrow(() -> new PlayerNotInFactionException(targetPlayer));

        final boolean isCancelled = EventRunner.runFactionDemoteEventPre(player, targetPlayer, faction);
        if (isCancelled)
            return targetPlayer.getFactionRole();

        FactionMemberType demotedTo = FactionMemberType.RECRUIT;
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());

        if(members.contains(targetPlayer.getUniqueId()))
        {
            recruits.add(targetPlayer.getUniqueId());
            members.remove(targetPlayer.getUniqueId());
        }
        else if (officers.contains(targetPlayer.getUniqueId()))
        {
            members.add(targetPlayer.getUniqueId());
            officers.remove(targetPlayer.getUniqueId());
            demotedTo = FactionMemberType.MEMBER;
        }

        final Faction updatedFaction = faction.toBuilder()
                .setRecruits(recruits)
                .setOfficers(officers)
                .setMembers(members)
                .build();
        this.storageManager.saveFaction(updatedFaction);

        //Update player
        final FactionPlayer factionPlayer = this.storageManager.getPlayer(targetPlayer.getUniqueId());
        if(factionPlayer != null)
        {
            this.storageManager.savePlayer(new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), updatedFaction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone()));
            EventRunner.runFactionDemoteEventPost(player, targetPlayer, demotedTo, updatedFaction);
        }

        return demotedTo;
    }

    @Override
    public FactionMemberType promotePlayer(final @Nullable ServerPlayer player, FactionPlayer targetPlayer) throws PlayerNotInFactionException
    {
        checkNotNull(targetPlayer);

        Faction faction = targetPlayer.getFaction()
                .orElseThrow(() -> new PlayerNotInFactionException(targetPlayer));

        final boolean isCancelled = EventRunner.runFactionPromoteEventPre(player, targetPlayer, faction);
        if (isCancelled)
            return targetPlayer.getFactionRole();

        FactionMemberType promotedTo = FactionMemberType.MEMBER;

        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> officers = new HashSet<>(faction.getOfficers());

        if(recruits.contains(targetPlayer.getUniqueId()))
        {
            members.add(targetPlayer.getUniqueId());
            recruits.remove(targetPlayer.getUniqueId());
        }
        else if (members.contains(targetPlayer.getUniqueId()))
        {
            officers.add(targetPlayer.getUniqueId());
            members.remove(targetPlayer.getUniqueId());
            promotedTo = FactionMemberType.OFFICER;
        }

        final Faction updatedFaction = faction.toBuilder()
                .setRecruits(recruits)
                .setOfficers(officers)
                .setMembers(members)
                .build();
        this.storageManager.saveFaction(updatedFaction);

        //Update player
        final FactionPlayer factionPlayer = this.storageManager.getPlayer(targetPlayer.getUniqueId());
        if (factionPlayer != null)
        {
            final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), updatedFaction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
            this.storageManager.savePlayer(updatedPlayer);
            EventRunner.runFactionPromoteEventPost(player, factionPlayer, promotedTo, updatedFaction);
        }

        return promotedTo;
    }

    @Override
    public boolean setLeader(final @Nullable FactionPlayer targetPlayer, final Faction faction)
    {
        checkNotNull(faction);

        final Set<UUID> officers = new HashSet<>(faction.getOfficers());
        final Set<UUID> members = new HashSet<>(faction.getMembers());
        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());

        UUID newLeaderUUID;

        if(!faction.getLeader().equals(new UUID(0, 0)))
        {
            officers.add(faction.getLeader());
        }

        if (targetPlayer == null)
        {
            newLeaderUUID = new UUID(0, 0);
        }
        else
        {
            newLeaderUUID = targetPlayer.getUniqueId();

            if(faction.getOfficers().contains(targetPlayer.getUniqueId()))
            {
                officers.remove(targetPlayer.getUniqueId());
            }
            else if(faction.getMembers().contains(targetPlayer.getUniqueId()))
            {
                members.remove(targetPlayer.getUniqueId());
            }
            else if(faction.getRecruits().contains(targetPlayer.getUniqueId()))
            {
                recruits.remove(targetPlayer.getUniqueId());
            }
        }

        final Faction updatedFaction = faction.toBuilder()
                .setLeader(newLeaderUUID)
                .setOfficers(officers)
                .setMembers(members)
                .setRecruits(recruits)
                .build();

        storageManager.saveFaction(updatedFaction);
        return true;
    }

//    private List<FactionMemberType> getDemotableRolesForRole(FactionMemberType factionMemberType)
//    {
//        if (factionMemberType != FactionMemberType.LEADER && factionMemberType != FactionMemberType.OFFICER)
//            return Collections.emptyList();
//
//        //In case we want to add more roles in the future (probably, we will)
//        List<FactionMemberType> roles = new ArrayList<>(Arrays.asList(FactionMemberType.values()));
//        roles.remove(FactionMemberType.ALLY);
//        roles.remove(FactionMemberType.TRUCE);
//        roles.remove(FactionMemberType.RECRUIT);
//        roles.remove(FactionMemberType.NONE);
//
//        if (factionMemberType == FactionMemberType.LEADER)
//        {
//            roles.remove(FactionMemberType.LEADER);
//        }
//        else
//        {
//            roles.remove(FactionMemberType.LEADER);
//            roles.remove(FactionMemberType.OFFICER);
//        }
//        return roles;
//    }
//
//    private List<FactionMemberType> getPromotableRolesForRole(FactionMemberType factionMemberType)
//    {
//        if (factionMemberType != FactionMemberType.LEADER && factionMemberType != FactionMemberType.OFFICER)
//            return Collections.emptyList();
//
//        //In case we want to add more roles in the future (probably, we will)
//        List<FactionMemberType> roles = new ArrayList<>(Arrays.asList(FactionMemberType.values()));
//        roles.remove(FactionMemberType.ALLY);
//        roles.remove(FactionMemberType.TRUCE);
//        roles.remove(FactionMemberType.OFFICER);
//        roles.remove(FactionMemberType.LEADER);
//        roles.remove(FactionMemberType.NONE);
//
//        if (factionMemberType == FactionMemberType.OFFICER)
//        {
//            roles.remove(FactionMemberType.MEMBER);
//        }
//        return roles;
//    }
}
