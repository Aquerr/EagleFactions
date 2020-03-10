package io.github.aquerr.eaglefactions.common.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermType;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class PermsManagerImpl implements PermsManager
{
    public PermsManagerImpl()
    {

    }

    @Override
    public boolean canBreakBlock(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction)
    {
        return checkFlag(playerUUID, playerFaction, chunkFaction, FactionPermType.DESTROY);
    }

    @Override
    public boolean canPlaceBlock(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction)
    {
        return checkFlag(playerUUID, playerFaction, chunkFaction, FactionPermType.PLACE);
    }

    @Override
    public boolean canInteract(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction)
    {
        return checkFlag(playerUUID, playerFaction, chunkFaction, FactionPermType.USE);
    }

    @Override
    public boolean canClaim(final UUID playerUUID, final Faction playerFaction)
    {
        return checkFlag(playerUUID, playerFaction, FactionPermType.CLAIM);
    }

    @Override
    public boolean canAttack(final UUID playerUUID, final Faction playerFaction)
    {
        return checkFlag(playerUUID, playerFaction, FactionPermType.CLAIM);
    }

    @Override
    public boolean canInvite(final UUID playerUUID, final Faction playerFaction)
    {
        return checkFlag(playerUUID, playerFaction, FactionPermType.INVITE);
    }

    private boolean checkFlag(final UUID playerUUID, final Faction playerFaction, final FactionPermType flagTypes)
    {
        final FactionMemberType memberType = playerFaction.getPlayerMemberType(playerUUID);
        return playerFaction.getPerms().get(memberType).get(flagTypes);
    }

    private boolean checkFlag(final UUID playerUUID, final Faction playerFaction, final Faction chunkFaction, final FactionPermType flagType)
    {
        if (playerFaction.getName().equals(chunkFaction.getName()))
        {
            final FactionMemberType memberType = chunkFaction.getPlayerMemberType(playerUUID);
            return chunkFaction.getPerms().get(memberType).get(flagType);
        }
        else if (playerFaction.getAlliances().contains(chunkFaction.getName()))
        {
            return chunkFaction.getPerms().get(FactionMemberType.ALLY).get(flagType);
        }
        else if (playerFaction.getTruces().contains(chunkFaction.getName()))
        {
            return chunkFaction.getPerms().get(FactionMemberType.TRUCE).get(flagType);
        }
        return false;
    }
}
