package io.github.aquerr.eaglefactions.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.entities.RankImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class PermsManagerImpl implements PermsManager
{
    @Override
    public boolean canBreakBlock(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final Claim claim)
    {
        return checkPermission(playerUUID, playerFaction, chunkFaction, FactionPermission.BLOCK_DESTROY, claim);
    }

    @Override
    public boolean canPlaceBlock(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final Claim claim)
    {
        return checkPermission(playerUUID, playerFaction, chunkFaction, FactionPermission.BLOCK_PLACE, claim);
    }

    @Override
    public boolean canInteract(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final Claim claim)
    {
        return checkPermission(playerUUID, playerFaction, chunkFaction, FactionPermission.INTERACT, claim);
    }

    @Override
    public boolean canClaim(final UUID playerUUID, final Faction playerFaction)
    {
        return hasPermission(playerUUID, playerFaction, FactionPermission.TERRITORY_CLAIM);
    }

    @Override
    public boolean canAttack(final UUID playerUUID, final Faction playerFaction)
    {
        return hasPermission(playerUUID, playerFaction, FactionPermission.ATTACK);
    }

    @Override
    public boolean canInvite(final UUID playerUUID, final Faction playerFaction)
    {
        return hasPermission(playerUUID, playerFaction, FactionPermission.INVITE_PLAYERS);
    }

    @Override
    public boolean canUseChest(final UUID playerUUID, final Faction playerFaction)
    {
        return hasPermission(playerUUID, playerFaction, FactionPermission.VIEW_FACTION_CHEST);
    }

    @Override
    public boolean hasPermission(UUID playerUUID, Faction playerFaction, FactionPermission permission)
    {
        return getPlayerPermissions(playerUUID, playerFaction)
                .contains(permission);
    }

    private boolean checkPermission(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final FactionPermission permission, final Claim claim)
    {
        RelationType relationType = chunkFaction.getRelationTo(playerFaction);
        if (relationType == RelationType.SAME_FACTION)
        {
            final Set<FactionPermission> playerRankPermissions = getPlayerPermissions(playerUUID, chunkFaction);
            boolean hasPerm = playerRankPermissions.contains(permission);
            if (!hasPerm)
            {
                return false;
            }

            if (playerRankPermissions.contains(FactionPermission.INTERNAL_CLAIM_BYPASS_ACCESS))
                return true;

            final boolean isAccessibleByFaction = claim.isAccessibleByFaction();
            if (isAccessibleByFaction) // Is public internal claim
                return true;
            else return claim.hasAccess(playerUUID); // Else if private, check if player has permission for claim.
        }
        else
        {
            return chunkFaction.getRelationPermissions(relationType).contains(permission) && claim.isAccessibleByFaction();
        }
    }

    private Set<FactionPermission> getPlayerPermissions(final UUID playerUUID, Faction faction)
    {
        return faction.getPlayerRanks(playerUUID).stream()
                .map(Rank::getPermissions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public static Set<FactionPermission> getDefaultAlliancePermissions()
    {
        return Set.of(FactionPermission.INTERACT, FactionPermission.BLOCK_PLACE, FactionPermission.BLOCK_DESTROY);
    }

    public static Set<FactionPermission> getDefaultTrucePermissions()
    {
        return Set.of(FactionPermission.INTERACT);
    }

}
