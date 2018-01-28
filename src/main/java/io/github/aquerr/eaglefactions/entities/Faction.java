package io.github.aquerr.eaglefactions.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    }
}
