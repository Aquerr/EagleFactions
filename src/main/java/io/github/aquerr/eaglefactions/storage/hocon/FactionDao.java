//package io.github.aquerr.eaglefactions.storage.hocon;
//
//import io.github.aquerr.eaglefactions.entities.FactionFlagTypes;
//import io.github.aquerr.eaglefactions.entities.FactionHome;
//import io.github.aquerr.eaglefactions.entities.FactionMemberType;
//import io.github.aquerr.eaglefactions.managers.FlagManager;
//import org.spongepowered.api.text.Text;
//import org.spongepowered.api.text.format.TextColors;
//
//import java.time.Instant;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//
//public class FactionDao
//{
//    private String name;
//    private Text tag;
//    //public BigDecimal Power;
//    private Set<String> recruits;
//    private Set<String> members;
//    private Set<String> alliances;
//    private Set<String> enemies;
//    private UUID leader;
//    private Set<String> officers;
//    private Set<String> claims;
//    private FactionHome home;
//    private Instant lastOnline;
//    private Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags;
//
//    //Constructor used while creating a new faction.
//    public FactionDao(String factionName, String factionTag, UUID factionLeader)
//    {
//        this.name = factionName;
//        this.tag = Text.of(TextColors.GREEN, factionTag);
//        this.leader = factionLeader;
//        //this.Power = new BigDecimal("0.0");
//        this.recruits = new HashSet<>();
//        this.members = new HashSet<>();
//        this.claims = new HashSet<>();
//        this.officers = new HashSet<>();
//        this.alliances = new HashSet<>();
//        //TODO: Add truce
//        this.enemies = new HashSet<>();
//        this.home = null;
//        this.lastOnline = Instant.now();
//        this.flags = FlagManager.getDefaultFactionFlags();
//    }
//
//    //Constructor used while getting a faction from storage.
//    public Faction(String factionName, Text factionTag, UUID factionLeader, Set<UUID> recruits, Set<UUID> members, Set<String> claims, Set<UUID> officers, Set<String> alliances, Set<String> enemies, FactionHome home, Instant lastOnline, Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags)
//    {
//        this.name = factionName;
//        this.tag = factionTag;
//        this.leader = factionLeader;
//        //this.Power = new BigDecimal("0.0");
//        this.recruits = recruits;
//        this.members = members;
//        this.claims = claims;
//        this.officers = officers;
//        this.alliances = alliances;
//        this.enemies = enemies;
//        this.home = home;
//        this.lastOnline = lastOnline;
//        this.flags = flags;
//    }
//
//    public String getName()
//    {
//        return this.name;
//    }
//
//    public FactionHome getHome()
//    {
//        return this.home;
//    }
//
//    public void setHome(FactionHome home)
//    {
//        this.home = home;
//    }
//
//    public Set<String> getAlliances()
//    {
//        return this.alliances;
//    }
//
//    public boolean addAlliance(String factionName)
//    {
//        return this.alliances.add(factionName);
//    }
//
//    public boolean removeAlliance(String factionName)
//    {
//        return this.alliances.remove(factionName);
//    }
//
//    public Set<String> getClaims()
//    {
//        return this.claims;
//    }
//
//    public boolean addClaim(String claim)
//    {
//        return this.claims.add(claim);
//    }
//
//    public boolean removeClaim(String claim)
//    {
//        return this.claims.remove(claim);
//    }
//
//    public void removeAllClaims()
//    {
//        this.claims.clear();
//    }
//
//    public Set<String> getEnemies()
//    {
//        return enemies;
//    }
//
//    public boolean addEnemy(String factionName)
//    {
//        return this.enemies.add(factionName);
//    }
//
//    public boolean removeEnemy(String factionName)
//    {
//        return this.enemies.remove(factionName);
//    }
//
//    public Set<UUID> getMembers()
//    {
//        return this.members;
//    }
//
//    public boolean addMember(UUID playerUUID)
//    {
//        return this.members.add(playerUUID);
//    }
//
//    public boolean removeMember(UUID playerUUID)
//    {
//        return this.members.remove(playerUUID);
//    }
//
//    public Set<UUID> getOfficers()
//    {
//        return officers;
//    }
//
//    public boolean addOfficer(UUID playerUUID)
//    {
//        return this.officers.add(playerUUID);
//    }
//
//    public boolean removeOfficer(UUID playerUUID)
//    {
//        return this.officers.remove(playerUUID);
//    }
//
//    public Set<UUID> getRecruits()
//    {
//        return this.recruits;
//    }
//
//    public boolean addRecruit(UUID playerUUID)
//    {
//        return this.recruits.add(playerUUID);
//    }
//
//    public boolean removeRecruit(UUID playerUUID)
//    {
//        return this.recruits.remove(playerUUID);
//    }
//
//    public Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getFlags()
//    {
//        return flags;
//    }
//
//    public void setFlag(FactionMemberType factionMemberType, FactionFlagTypes factionFlagTypes, Boolean flagValue)
//    {
//        this.flags.get(factionMemberType).replace(factionFlagTypes, flagValue);
//    }
//
//    public UUID getLeader()
//    {
//        return this.leader;
//    }
//
//    public void setLeader(UUID playerUUID)
//    {
//        this.leader = playerUUID;
//    }
//
//    public Text getTag()
//    {
//        return this.tag;
//    }
//
//    public void setTag(Text tag)
//    {
//        this.tag = tag;
//    }
//
//    public Instant getLastOnline()
//    {
//        return this.lastOnline;
//    }
//
//    public void setLastOnline(Instant lastOnline)
//    {
//        this.lastOnline = lastOnline;
//    }
//}
