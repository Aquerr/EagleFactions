package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagType;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FlagManager
{
    public static boolean canBreakBlock(Player player, Faction playerFaction, Faction chunkFaction)
    {
        return checkFlag(player, playerFaction, chunkFaction, FactionFlagType.DESTROY);
    }

    public static boolean canPlaceBlock(Player player, Faction playerFactionName, Faction chunkFaction)
    {
        return checkFlag(player, playerFactionName, chunkFaction, FactionFlagType.PLACE);
    }

    public static boolean canInteract(Player player, Faction playerFactionName, Faction chunkFaction)
    {
        return checkFlag(player, playerFactionName, chunkFaction, FactionFlagType.USE);
    }

    private static boolean checkFlag(Player player, Faction playerFaction, Faction chunkFaction, FactionFlagType flagType)
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

    public static Map<FactionMemberType, Map<FactionFlagType, Boolean>> getDefaultFactionFlags()
    {
        Map<FactionMemberType, Map<FactionFlagType, Boolean>> map = new LinkedHashMap<>();
        Map<FactionFlagType, Boolean> leaderMap = new LinkedHashMap<>();
        Map<FactionFlagType, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionFlagType, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionFlagType, Boolean> allyMap = new LinkedHashMap<>();

        leaderMap.put(FactionFlagType.USE, true);
        leaderMap.put(FactionFlagType.PLACE, true);
        leaderMap.put(FactionFlagType.DESTROY, true);

        officerMap.put(FactionFlagType.USE, true);
        officerMap.put(FactionFlagType.PLACE, true);
        officerMap.put(FactionFlagType.DESTROY, true);

        membersMap.put(FactionFlagType.USE, true);
        membersMap.put(FactionFlagType.PLACE, true);
        membersMap.put(FactionFlagType.DESTROY, true);

        allyMap.put(FactionFlagType.USE, true);
        allyMap.put(FactionFlagType.PLACE, false);
        allyMap.put(FactionFlagType.DESTROY, false);

        map.put(FactionMemberType.LEADER, leaderMap);
        map.put(FactionMemberType.OFFICER, officerMap);
        map.put(FactionMemberType.MEMBER, membersMap);
        map.put(FactionMemberType.ALLY, allyMap);

        return map;
    }
}
