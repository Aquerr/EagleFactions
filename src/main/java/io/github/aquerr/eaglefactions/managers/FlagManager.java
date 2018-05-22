package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagTypes;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import org.spongepowered.api.entity.living.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class FlagManager
{
    public static boolean canBreakBlock(Player player, Faction playerFaction, Faction chunkFaction)
    {
        return checkFlag(player, playerFaction, chunkFaction, FactionFlagTypes.DESTROY);
    }

    public static boolean canPlaceBlock(Player player, Faction playerFactionName, Faction chunkFaction)
    {
        return checkFlag(player, playerFactionName, chunkFaction, FactionFlagTypes.PLACE);
    }

    public static boolean canInteract(Player player, Faction playerFactionName, Faction chunkFaction)
    {
        return checkFlag(player, playerFactionName, chunkFaction, FactionFlagTypes.USE);
    }

    private static boolean checkFlag(Player player, Faction playerFaction, Faction chunkFaction, FactionFlagTypes flagType)
    {

        if (playerFaction.Name.equals(chunkFaction.Name))
        {
            if (chunkFaction.Leader.equals(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.LEADER).get(flagType);
            }
            else if(chunkFaction.Officers.contains(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.OFFICER).get(flagType);
            }
            else if(chunkFaction.Members.contains(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.MEMBER).get(flagType);
            }
        }
        else if(playerFaction.Alliances.contains(chunkFaction.Name))
        {
            return chunkFaction.Flags.get(FactionMemberType.ALLY).get(flagType);
        }

        return false;
    }

    public static Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getDefaultFactionFlags()
    {
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> map = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> leaderMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> allyMap = new LinkedHashMap<>();

        leaderMap.put(FactionFlagTypes.USE, true);
        leaderMap.put(FactionFlagTypes.PLACE, true);
        leaderMap.put(FactionFlagTypes.DESTROY, true);
        leaderMap.put(FactionFlagTypes.ATTACK, true);
        leaderMap.put(FactionFlagTypes.INVITE, true);

        officerMap.put(FactionFlagTypes.USE, true);
        officerMap.put(FactionFlagTypes.PLACE, true);
        officerMap.put(FactionFlagTypes.DESTROY, true);
        officerMap.put(FactionFlagTypes.ATTACK, true);
        officerMap.put(FactionFlagTypes.INVITE, true);

        membersMap.put(FactionFlagTypes.USE, true);
        membersMap.put(FactionFlagTypes.PLACE, true);
        membersMap.put(FactionFlagTypes.DESTROY, true);
        membersMap.put(FactionFlagTypes.ATTACK, false);
        membersMap.put(FactionFlagTypes.INVITE, true);

        allyMap.put(FactionFlagTypes.USE, true);
        allyMap.put(FactionFlagTypes.PLACE, false);
        allyMap.put(FactionFlagTypes.DESTROY, false);

        map.put(FactionMemberType.LEADER, leaderMap);
        map.put(FactionMemberType.OFFICER, officerMap);
        map.put(FactionMemberType.MEMBER, membersMap);
        map.put(FactionMemberType.ALLY, allyMap);

        return map;
    }
}
