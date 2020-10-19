package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.*;

/**
 * The implementation of Faction interface.
 * FactionImpl is an immutable object. To change its values, use FactionLogic or Faction.Builder
 */
public class FactionImpl implements Faction
{
    private final String name;
    private final Text tag;
    private final String description;
    private final String messageOfTheDay;
    private final Set<UUID> recruits;
    private final Set<UUID> members;
    private final Set<String> truces;
    private final Set<String> alliances;
    private final Set<String> enemies;
    private final UUID leader;
    private final Set<UUID> officers;
    private final Set<Claim> claims;
    private final FactionHome home;
    private final Instant lastOnline;
    private final boolean isPublic;
    private final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms;

    private FactionChest chest;

    public FactionImpl(final BuilderImpl builder)
    {
        this.name = builder.name;
        this.tag = builder.tag;
        this.description = builder.description;
        this.messageOfTheDay = builder.messageOfTheDay;
        this.leader = builder.leader;
        this.recruits = builder.recruits;
        this.members = builder.members;
        this.claims = builder.claims;
        this.officers = builder.officers;
        this.truces = builder.truces;
        this.alliances = builder.alliances;
        this.enemies = builder.enemies;
        this.home = builder.home;
        this.lastOnline = builder.lastOnline;
        this.perms = builder.perms;
        this.chest = builder.chest;
        this.isPublic = builder.isPublic;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Text getTag()
    {
        return this.tag;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public String getMessageOfTheDay()
    {
        return this.messageOfTheDay;
    }

    @Override
    public FactionHome getHome()
    {
        return this.home;
    }

    @Override
    public Set<String> getTruces()
    {
        return Collections.unmodifiableSet(this.truces);
    }

    @Override
    public Set<String> getAlliances()
    {
        return Collections.unmodifiableSet(this.alliances);
    }

    @Override
    public Set<Claim> getClaims()
    {
        return Collections.unmodifiableSet(this.claims);
    }

    @Override
    public Set<String> getEnemies()
    {
        return Collections.unmodifiableSet(enemies);
    }

    @Override
    public Set<UUID> getMembers()
    {
        return Collections.unmodifiableSet(this.members);
    }

    @Override
    public Set<UUID> getOfficers()
    {
        return Collections.unmodifiableSet(officers);
    }

    @Override
    public Set<UUID> getRecruits()
    {
        return Collections.unmodifiableSet(this.recruits);
    }

    @Override
    public Set<UUID> getPlayers()
    {
        //This set does not need to unmodifiable as making changes in it won't affect the faction object.
        final Set<UUID> players = new HashSet<>();
        players.add(getLeader());
        players.addAll(getRecruits());
        players.addAll(getMembers());
        players.addAll(getOfficers());
        return players;
    }

    @Override
    public Map<FactionMemberType, Map<FactionPermType, Boolean>> getPerms()
    {
        return Collections.unmodifiableMap(perms);
    }

    @Override
    public UUID getLeader()
    {
        return this.leader;
    }

    @Override
    public Instant getLastOnline()
    {
        return this.lastOnline;
    }

    @Override
    public FactionMemberType getPlayerMemberType(final UUID playerUUID)
    {
        if (this.leader.equals(playerUUID))
            return FactionMemberType.LEADER;
        else if(this.officers.contains(playerUUID))
            return FactionMemberType.OFFICER;
        else if(this.members.contains(playerUUID))
            return FactionMemberType.MEMBER;
        else if(this.recruits.contains(playerUUID))
            return FactionMemberType.RECRUIT;
        else
        {
            final FactionLogic factionLogic = EagleFactionsPlugin.getPlugin().getFactionLogic();
            Optional<Faction> optionalFaction = this.alliances.stream()
                    .map(factionLogic::getFactionByName)
                    .filter(Objects::nonNull)
                    .filter(y->y.containsPlayer(playerUUID))
                    .findAny();
            if(optionalFaction.isPresent())
                return FactionMemberType.ALLY;

            optionalFaction = this.truces.stream().map(factionLogic::getFactionByName).filter(Objects::nonNull).filter(y->y.containsPlayer(playerUUID)).findAny();
            if(optionalFaction.isPresent()) return FactionMemberType.TRUCE;
        }
        return FactionMemberType.NONE;
    }

    @Override
    public FactionChest getChest()
    {
        return this.chest;
    }

    @Override
    public boolean isPublic()
    {
        return this.isPublic;
    }

    @Override
    public boolean containsPlayer(final UUID playerUUID)
    {
        if (this.leader.equals(playerUUID))
            return true;
        else if(this.officers.contains(playerUUID))
            return true;
        else if(this.members.contains(playerUUID))
            return true;
        else return this.recruits.contains(playerUUID);
    }

    @Override
    public Faction.Builder toBuilder()
    {
        final Faction.Builder factionBuilder = new BuilderImpl();
        factionBuilder.setName(this.name);
        factionBuilder.setTag(this.tag);
        factionBuilder.setDescription(this.description);
        factionBuilder.setMessageOfTheDay(this.messageOfTheDay);
        factionBuilder.setLeader(this.leader);
        factionBuilder.setOfficers(this.officers);
        factionBuilder.setMembers(this.members);
        factionBuilder.setRecruits(this.recruits);
        factionBuilder.setAlliances(this.alliances);
        factionBuilder.setEnemies(this.enemies);
        factionBuilder.setClaims(this.claims);
        factionBuilder.setLastOnline(this.lastOnline);
        factionBuilder.setHome(this.home);
        factionBuilder.setPerms(this.perms);
        factionBuilder.setChest(this.chest);
        factionBuilder.setIsPublic(this.isPublic);

        return factionBuilder;
    }

    public static Faction.Builder builder(final String name, final Text tag, final UUID leader)
    {
        return new BuilderImpl(name, tag, leader);
    }

    @Override
    public int compareTo(final Faction object)
    {
        return this.name.compareTo(object.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionImpl faction = (FactionImpl) o;
        return Objects.equals(name, faction.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    //Builder
    public static final class BuilderImpl implements Faction.Builder
    {
        private String name;
        private Text tag;
        private String description;
        private String messageOfTheDay;
        private UUID leader;
        private Set<UUID> recruits;
        private Set<UUID> members;
        private Set<String> truces;
        private Set<String> alliances;
        private Set<String> enemies;
        private Set<UUID> officers;
        private Set<Claim> claims;
        private FactionHome home;
        private Instant lastOnline;
        private Map<FactionMemberType, Map<FactionPermType, Boolean>> perms;
        private FactionChest chest;
        private boolean isPublic;

        private BuilderImpl()
        {
            this.description = "";
            this.messageOfTheDay = "";
            this.recruits = new HashSet<>();
            this.members = new HashSet<>();
            this.truces = new HashSet<>();
            this.alliances = new HashSet<>();
            this.enemies = new HashSet<>();
            this.officers = new HashSet<>();
            this.claims = new HashSet<>();
            this.home = null;
            this.isPublic = false;
        }

        public BuilderImpl(final String name, final Text tag, final UUID leader)
        {
            this();
            this.name = name;
            this.tag = tag;
            this.leader = leader;
            this.chest = new FactionChestImpl(this.name);
        }

        public Builder setName(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder setTag(final Text tag)
        {
            this.tag = tag;
            return this;
        }

        public Builder setDescription(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder setMessageOfTheDay(final String messageOfTheDay)
        {
            this.messageOfTheDay = messageOfTheDay;
            return this;
        }

        public Builder setLeader(final UUID leaderUUID)
        {
            this.leader = leaderUUID;
            return this;
        }

        public Builder setRecruits(final Set<UUID> recruits)
        {
            this.recruits = recruits;
            return this;
        }

        public Builder setMembers(final Set<UUID> members)
        {
            this.members = members;
            return this;
        }

        public Builder setOfficers(final Set<UUID> officers)
        {
            this.officers = officers;
            return this;
        }

        public Builder setTruces(final Set<String> truces)
        {
            this.truces = truces;
            return this;
        }

        public Builder setAlliances(final Set<String> alliances)
        {
            this.alliances = alliances;
            return this;
        }

        public Builder setEnemies(final Set<String> enemies)
        {
            this.enemies = enemies;
            return this;
        }

        public Builder setClaims(final Set<Claim> claims)
        {
            this.claims = claims;
            return this;
        }

        public Builder setHome(final FactionHome home)
        {
            this.home = home;
            return this;
        }

        public Builder setLastOnline(final Instant lastOnline)
        {
            this.lastOnline = lastOnline;
            return this;
        }

        public Builder setPerms(final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms)
        {
            this.perms = perms;
            return this;
        }

        public Builder setChest(final FactionChest chest)
        {
            this.chest = chest;
            return this;
        }

        public Builder setIsPublic(final boolean isPublic)
        {
            this.isPublic = isPublic;
            return this;
        }

        public Faction build()
        {
            if(this.name == null || this.tag == null || this.leader == null)
                throw new IllegalStateException("Couldn't build Faction object! Faction must have a name, a tag and a leader!");

            if(this.lastOnline == null)
                this.lastOnline = Instant.now();
            if(this.perms == null)
                this.perms = PermsManager.getDefaultFactionPerms();
            if(this.chest == null)
                this.chest = new FactionChestImpl(this.name);

            return new FactionImpl(this);
        }
    }
}
