package io.github.aquerr.eaglefactions.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPermType;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;

import java.util.UUID;

@Singleton
public class PermsManagerImpl implements PermsManager
{
    public PermsManagerImpl()
    {

    }

    @Override
    public boolean canBreakBlock(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final Claim claim)
    {
        return checkPermission(playerUUID, playerFaction, chunkFaction, FactionPermType.DESTROY, claim);
    }

    @Override
    public boolean canPlaceBlock(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final Claim claim)
    {
        return checkPermission(playerUUID, playerFaction, chunkFaction, FactionPermType.PLACE, claim);
    }

    @Override
    public boolean canInteract(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final Claim claim)
    {
        return checkPermission(playerUUID, playerFaction, chunkFaction, FactionPermType.USE, claim);
    }

    @Override
    public boolean canClaim(final UUID playerUUID, final Faction playerFaction)
    {
        return checkPermission(playerUUID, playerFaction, FactionPermType.CLAIM);
    }

    @Override
    public boolean canAttack(final UUID playerUUID, final Faction playerFaction)
    {
        return checkPermission(playerUUID, playerFaction, FactionPermType.CLAIM);
    }

    @Override
    public boolean canInvite(final UUID playerUUID, final Faction playerFaction)
    {
        return checkPermission(playerUUID, playerFaction, FactionPermType.INVITE);
    }

    private boolean checkPermission(final UUID playerUUID, final Faction playerFaction, final FactionPermType flagTypes)
    {
        final FactionMemberType memberType = playerFaction.getPlayerMemberType(playerUUID);
        if (memberType == FactionMemberType.LEADER)
            return true;
        return playerFaction.getPerms().get(memberType).get(flagTypes);
    }

    private boolean checkPermission(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final FactionPermType flagType, final Claim claim)
    {
        if (playerFaction.getName().equals(chunkFaction.getName()))
        {
            final FactionMemberType memberType = chunkFaction.getPlayerMemberType(playerUUID);

            //Leaders has permission for everything.
            if (memberType == FactionMemberType.LEADER)
                return true;
            if (memberType == FactionMemberType.NONE)
                return false;

            final boolean hasPerm = chunkFaction.getPerms().get(memberType).get(flagType);
            if (hasPerm) //If player has perms specified in /f perms, then we need to check for internal claiming
            {
                if (memberType == FactionMemberType.OFFICER) //Officers are like vice-leaders. They should have access to internal claims.
                    return true;

                final boolean isAccessibleByFaction = claim.isAccessibleByFaction();
                if (isAccessibleByFaction)
                    return true;
                else return claim.hasAccess(playerUUID);
            }
            else return false;
        }
        else if (playerFaction.getAlliances().contains(chunkFaction.getName()))
        {
            final boolean hasPerms = chunkFaction.getPerms().get(FactionMemberType.ALLY).get(flagType);
            return hasPerms && claim.isAccessibleByFaction(); //If faction has access then allies have it as well.
        }
        else if (playerFaction.getTruces().contains(chunkFaction.getName()))
        {
            //Truces cannot break, nor place or interact with blocks.
            return false;
        }
        return false;
    }
}
