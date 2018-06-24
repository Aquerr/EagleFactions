package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.managers.FlagManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Aquerr on 2017-07-13.
 */
public class Faction
{
    public String Name;
    public Text Tag;
    //public BigDecimal Power;
    public List<String> Recruits;
    public List<String> Members;
    public List<String> Alliances;
    public List<String> Enemies;
    public String Leader;
    public List<String> Officers;
    public List<String> Claims;
    public FactionHome Home;
    public Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> Flags;

    //Constructor used while creating a new faction.
    public Faction(String factionName, String factionTag, String factionLeader)
    {
        this.Name = factionName;
        this.Tag = Text.of(TextColors.GREEN, factionTag);
        this.Leader = factionLeader;
        //this.Power = new BigDecimal("0.0");
        this.Recruits = new ArrayList<>();
        this.Members = new ArrayList<>();
        this.Claims = new ArrayList<>();
        this.Officers = new ArrayList<>();
        this.Alliances = new ArrayList<>();
        this.Enemies = new ArrayList<>();
        this.Home = null;
        this.Flags = FlagManager.getDefaultFactionFlags();
    }

    //Constructor used while getting a faction from storage.
    public Faction(String factionName, Text factionTag, String factionLeader, List<String> recruits, List<String> members, List<String> claims, List<String> officers, List<String> alliances, List<String> enemies, FactionHome home, Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags)
    {
        this.Name = factionName;
        this.Tag = factionTag;
        this.Leader = factionLeader;
        //this.Power = new BigDecimal("0.0");
        this.Recruits = recruits;
        this.Members = members;
        this.Claims = claims;
        this.Officers = officers;
        this.Alliances = alliances;
        this.Enemies = enemies;
        this.Home = home;
        this.Flags = flags;
    }
}
