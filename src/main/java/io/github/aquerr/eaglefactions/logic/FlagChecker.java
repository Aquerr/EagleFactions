package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagType;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import org.spongepowered.api.entity.living.player.Player;

public class FlagChecker
{
    public static boolean canBreakBlock(Player player, String playerFactionName, String chunkFactionName)
    {
        Faction chunkFaction = FactionLogic.getFaction(chunkFactionName);

        if (playerFactionName.equals(chunkFactionName))
        {
            if (chunkFaction.Leader.equals(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.LEADER).get(FactionFlagType.DESTROY);
            }
            else if(chunkFaction.Officers.contains(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.OFFICER).get(FactionFlagType.DESTROY);
            }
            else if(chunkFaction.Members.contains(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.MEMBER).get(FactionFlagType.DESTROY);
            }
        }
        else if(FactionLogic.getFaction(playerFactionName).Alliances.contains(chunkFaction.Name))
        {
            return chunkFaction.Flags.get(FactionMemberType.ALLY).get(FactionFlagType.DESTROY);
        }

        return false;
    }

    public static boolean canPlaceBlock(Player player, String playerFactionName, String chunkFactionName)
    {
        Faction chunkFaction = FactionLogic.getFaction(chunkFactionName);

        if (playerFactionName.equals(chunkFactionName))
        {
            if (chunkFaction.Leader.equals(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.LEADER).get(FactionFlagType.DESTROY);
            }
            else if(chunkFaction.Officers.contains(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.OFFICER).get(FactionFlagType.DESTROY);
            }
            else if(chunkFaction.Members.contains(player.getUniqueId().toString()))
            {
                return chunkFaction.Flags.get(FactionMemberType.MEMBER).get(FactionFlagType.DESTROY);
            }
        }
        else if(FactionLogic.getFaction(playerFactionName).Alliances.contains(chunkFaction.Name))
        {
            return chunkFaction.Flags.get(FactionMemberType.ALLY).get(FactionFlagType.DESTROY);
        }

        return false;
    }
}
