package io.github.aquerr.eaglefactions.api.managers;

import io.github.aquerr.eaglefactions.api.entities.Faction;

import java.util.UUID;

public interface IFlagManager
{
    boolean canBreakBlock(UUID playerUUID, Faction playerFaction, Faction chunkFaction);
    boolean canPlaceBlock(UUID playerUUID, Faction playerFaction, Faction chunkFaction);
    boolean canInteract(UUID playerUUID, Faction playerFaction, Faction chunkFaction);
    boolean canClaim(UUID playerUUID, Faction playerFaction);
    boolean canAttack(UUID playerUUID, Faction playerFaction);
    boolean canInvite(UUID playerUUID, Faction playerFaction);
}
