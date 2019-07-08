package io.github.aquerr.eaglefactions.api.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionFlagTypes;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class FlagManager implements IFlagManager
{
    private static FlagManager INSTANCE = null;
    private final EagleFactions plugin;

    private FlagManager(EagleFactions plugin)
    {
        this.plugin = plugin;
        INSTANCE = this;
    }

    public static FlagManager getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new FlagManager(eagleFactions);
        else return INSTANCE;
    }

    public boolean canBreakBlock(UUID playerUUID, Faction playerFaction, Faction chunkFaction)
    {
        return checkFlag(playerUUID, playerFaction, chunkFaction, FactionFlagTypes.DESTROY);
    }

    public boolean canPlaceBlock(UUID playerUUID, Faction playerFaction, Faction chunkFaction)
    {
        return checkFlag(playerUUID, playerFaction, chunkFaction, FactionFlagTypes.PLACE);
    }

    public boolean canInteract(UUID playerUUID, Faction playerFaction, Faction chunkFaction)
    {
        return checkFlag(playerUUID, playerFaction, chunkFaction, FactionFlagTypes.USE);
    }

    public boolean canClaim(UUID playerUUID, Faction playerFaction)
    {
        return checkFlag(playerUUID, playerFaction, FactionFlagTypes.CLAIM);
    }

    public boolean canAttack(UUID playerUUID, Faction playerFaction)
    {
        return checkFlag(playerUUID, playerFaction, FactionFlagTypes.CLAIM);
    }

    public boolean canInvite(UUID playerUUID, Faction playerFaction)
    {
        return checkFlag(playerUUID, playerFaction, FactionFlagTypes.INVITE);
    }

    private boolean checkFlag(UUID playerUUID, Faction playerFaction, FactionFlagTypes flagTypes)
    {
        FactionMemberType memberType = plugin.getPlayerManager().getFactionMemberType(playerUUID, playerFaction);

        return playerFaction.getFlags().get(memberType).get(flagTypes);
    }

    private boolean checkFlag(UUID playerUUID, Faction playerFaction, Faction chunkFaction, FactionFlagTypes flagType)
    {
        if (playerFaction.getName().equals(chunkFaction.getName()))
        {
            FactionMemberType memberType = plugin.getPlayerManager().getFactionMemberType(playerUUID, playerFaction);

            return chunkFaction.getFlags().get(memberType).get(flagType);
        }
        else if (playerFaction.getAlliances().contains(chunkFaction.getName()))
        {
            return chunkFaction.getFlags().get(FactionMemberType.ALLY).get(flagType);
        }
        else
        {
            return false;
        }
    }

    public static Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getDefaultFactionFlags()
    {
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> map = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> leaderMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> recruitsMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> allyMap = new LinkedHashMap<>();

        leaderMap.put(FactionFlagTypes.USE, true);
        leaderMap.put(FactionFlagTypes.PLACE, true);
        leaderMap.put(FactionFlagTypes.DESTROY, true);
        leaderMap.put(FactionFlagTypes.CLAIM, true);
        leaderMap.put(FactionFlagTypes.ATTACK, true);
        leaderMap.put(FactionFlagTypes.INVITE, true);

        officerMap.put(FactionFlagTypes.USE, true);
        officerMap.put(FactionFlagTypes.PLACE, true);
        officerMap.put(FactionFlagTypes.DESTROY, true);
        officerMap.put(FactionFlagTypes.CLAIM, true);
        officerMap.put(FactionFlagTypes.ATTACK, true);
        officerMap.put(FactionFlagTypes.INVITE, true);

        membersMap.put(FactionFlagTypes.USE, true);
        membersMap.put(FactionFlagTypes.PLACE, true);
        membersMap.put(FactionFlagTypes.DESTROY, true);
        membersMap.put(FactionFlagTypes.CLAIM, false);
        membersMap.put(FactionFlagTypes.ATTACK, false);
        membersMap.put(FactionFlagTypes.INVITE, true);

        recruitsMap.put(FactionFlagTypes.USE, true);
        recruitsMap.put(FactionFlagTypes.PLACE, true);
        recruitsMap.put(FactionFlagTypes.DESTROY, true);
        recruitsMap.put(FactionFlagTypes.CLAIM, false);
        recruitsMap.put(FactionFlagTypes.ATTACK, false);
        recruitsMap.put(FactionFlagTypes.INVITE, false);

        allyMap.put(FactionFlagTypes.USE, true);
        allyMap.put(FactionFlagTypes.PLACE, false);
        allyMap.put(FactionFlagTypes.DESTROY, false);

        map.put(FactionMemberType.LEADER, leaderMap);
        map.put(FactionMemberType.OFFICER, officerMap);
        map.put(FactionMemberType.MEMBER, membersMap);
        map.put(FactionMemberType.RECRUIT, recruitsMap);
        map.put(FactionMemberType.ALLY, allyMap);

        return map;
    }
}
