package io.github.aquerr.eaglefactions.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aquerr on 2017-07-13.
 */
public class Faction
{
    public String Name;
    public String Tag;
    public BigDecimal Power;
    public List<String> Members;
    public List<String> Alliances;
    public List<String> Enemies;
    public String Leader;
    public List<String> Officers;
    public List<String> Claims;
    public String Home;
    public Map<FactionMemberType, Map<FactionFlagType, Boolean>> Flags;

    public Faction(String factionName, String factionTag, String factionLeader)
    {
        this.Name = factionName;
        this.Tag = factionTag;
        this.Leader = factionLeader;
        this.Power = new BigDecimal("0.0");
        this.Members = new ArrayList<>();
        this.Claims = new ArrayList<>();
        this.Officers = new ArrayList<>();
        this.Alliances = new ArrayList<>();
        this.Enemies = new ArrayList<>();
        this.Home = "";

        Map<FactionMemberType, Map<FactionFlagType, Boolean>> map = new HashMap<>();
        Map<FactionFlagType, Boolean> leaderMap = new HashMap<>();
        Map<FactionFlagType, Boolean> officerMap = new HashMap<>();
        Map<FactionFlagType, Boolean> membersMap = new HashMap<>();
        Map<FactionFlagType, Boolean> allyMap = new HashMap<>();

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

        this.Flags = map;
    }
}
