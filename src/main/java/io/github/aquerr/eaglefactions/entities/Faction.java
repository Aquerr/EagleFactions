package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.managers.FlagManager;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.Inventory2D;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;

/**
 * Created by Aquerr on 2017-07-13.
 */
public class Faction
{
    private final String name;
    private final Text tag;
    private final String description;
    private final String messageOfTheDay;
    //public BigDecimal Power;
    private final Set<UUID> recruits;
    private final Set<UUID> members;
    private final Set<String> alliances;
    private final Set<String> enemies;
    private final UUID leader;
    private final Set<UUID> officers;
    private final Set<Claim> claims;
    private final FactionHome home;
    private final Instant lastOnline;
    private final Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags;

    private FactionChest chest;

    public Faction(final Builder builder)
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
        this.alliances = builder.alliances;
        this.enemies = builder.enemies;
        this.home = builder.home;
        this.lastOnline = builder.lastOnline;
        this.flags = builder.flags;
        this.chest = builder.chest;
    }

    public String getName()
    {
        return this.name;
    }

    public FactionHome getHome()
    {
        return this.home;
    }

    public Set<String> getAlliances()
    {
        return Collections.unmodifiableSet(this.alliances);
    }

    public Set<Claim> getClaims()
    {
        return Collections.unmodifiableSet(this.claims);
    }

    public Set<String> getEnemies()
    {
        return Collections.unmodifiableSet(enemies);
    }

    public Set<UUID> getMembers()
    {
        return Collections.unmodifiableSet(this.members);
    }

    public Set<UUID> getOfficers()
    {
        return Collections.unmodifiableSet(officers);
    }

    public Set<UUID> getRecruits()
    {
        return Collections.unmodifiableSet(this.recruits);
    }

    public Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getFlags()
    {
        return Collections.unmodifiableMap(flags);
    }

    public UUID getLeader()
    {
        return this.leader;
    }

    public Text getTag()
    {
        return this.tag;
    }

    public Instant getLastOnline()
    {
        return this.lastOnline;
    }

    public FactionMemberType getPlayerMemberType(UUID playerUUID)
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
            return null;
    }

    public FactionChest getChest()
    {
        return this.chest;
    }

    public boolean containsPlayer(UUID playerUUID)
    {
        if (this.leader.equals(playerUUID))
            return true;
        else if(this.officers.contains(playerUUID))
            return true;
        else if(this.members.contains(playerUUID))
            return true;
        else if(this.recruits.contains(playerUUID))
            return true;
        else
            return false;
    }

    public Builder toBuilder()
    {
        Builder factionBuilder = new Builder();
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
        factionBuilder.setFlags(this.flags);
        factionBuilder.setChest(this.chest);

        return factionBuilder;
    }

    public static Builder builder(final String name, final Text tag, final UUID leader)
    {
        return new Builder(name, tag, leader);
    }

    //Builder
    public static final class Builder
    {
        private String name;
        private Text tag;
        private String description;
        private String messageOfTheDay;
        private UUID leader;
        private Set<UUID> recruits;
        private Set<UUID> members;
        private Set<String> alliances;
        private Set<String> enemies;
        private Set<UUID> officers;
        private Set<Claim> claims;
        private FactionHome home;
        private Instant lastOnline;
        private Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags;
        private FactionChest chest;

        private Builder()
        {
            this.description = "";
            this.messageOfTheDay = "";
            this.recruits = new HashSet<>();
            this.members = new HashSet<>();
            this.alliances = new HashSet<>();
            this.enemies = new HashSet<>();
            this.officers = new HashSet<>();
            this.claims = new HashSet<>();
            this.home = null;
        }

        public Builder(final String name, final Text tag, final UUID leader)
        {
            this();
            this.name = name;
            this.tag = tag;
            this.leader = leader;
            this.chest = new FactionChest(this.name);
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

        public Builder setFlags(final Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags)
        {
            this.flags = flags;
            return this;
        }

        public Builder setChest(final FactionChest chest)
        {
            this.chest = chest;
            return this;
        }

        public Faction build()
        {
            if(this.name == null || this.tag == null || this.leader == null)
            {
                throw new IllegalStateException("Couldn't build FACTION object! FACTION must have a name, a tag and a leader.");
            }

            if(this.lastOnline == null)
            {
                this.lastOnline = Instant.now();
            }
            if(this.flags == null)
            {
                this.flags = FlagManager.getDefaultFactionFlags();
            }
            if(this.chest == null)
            {
                this.chest = new FactionChest(this.name);
            }

            return new Faction(this);
        }
    }
}
