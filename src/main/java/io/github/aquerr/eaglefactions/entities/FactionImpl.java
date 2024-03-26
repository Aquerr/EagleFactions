package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlags;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.managers.PermsManagerImpl;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import net.kyori.adventure.text.TextComponent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The implementation of Faction interface.
 * FactionImpl is an immutable object. To change its values, use FactionLogic or Faction.Builder
 */
public class FactionImpl implements Faction
{
    private final String name;
    private final TextComponent tag;
    private final String description;
    private final String messageOfTheDay;
    private final Map<UUID, FactionMember> members;
    private final Set<String> truces;
    private final Set<String> alliances;
    private final Set<String> enemies;
    private final UUID leader;
    private final Set<Claim> claims;
    private final FactionHome home;
    private final Instant lastOnline;
    private final Instant createdDate;
    private final boolean isPublic;

    private final Map<RelationType, Set<FactionPermission>> relationPermissions;

    private final List<Rank> ranks;
    private final FactionChest chest;

    private final ProtectionFlags protectionFlags;

    public FactionImpl(final BuilderImpl builder)
    {
        this.name = builder.name;
        this.tag = builder.tag;
        this.description = builder.description;
        this.messageOfTheDay = builder.messageOfTheDay;
        this.leader = builder.leader;
        this.members = builder.members.stream()
                .collect(Collectors.toMap(FactionMember::getUniqueId, Function.identity(), (member, member2) -> member));
        this.claims = builder.claims;
        this.truces = builder.truces;
        this.alliances = builder.alliances;
        this.enemies = builder.enemies;
        this.home = builder.home;
        this.lastOnline = builder.lastOnline;
        this.createdDate = builder.createdDate;
        this.chest = builder.chest;
        this.isPublic = builder.isPublic;
        this.ranks = builder.ranks;
        this.relationPermissions = Map.of(
                RelationType.ALLIANCE, builder.alliancePermissions,
                RelationType.TRUCE, builder.trucePermissions
        );
        this.protectionFlags = new ProtectionFlagsImpl(builder.protectionFlags);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public TextComponent getTag()
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
    public Optional<FactionHome> getHome()
    {
        return Optional.ofNullable(this.home);
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
    public List<Rank> getRanks()
    {
        return Collections.unmodifiableList(this.ranks);
    }

    @Override
    public Rank getDefaultRank()
    {
        return this.getRanks().stream()
                .filter(rank -> rank.getName().equalsIgnoreCase(RankManagerImpl.DEFAULT_RANK_NAME))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Rank getLeaderRank()
    {
        return this.getRanks().stream()
                .filter(rank -> rank.getName().equalsIgnoreCase(RankManagerImpl.LEADER_RANK_NAME))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Rank> getRank(String rankName)
    {
        return this.ranks.stream()
                .filter(rank -> rank.getName().equalsIgnoreCase(rankName))
                .findFirst();
    }

    @Override
    public Set<FactionMember> getMembers()
    {
        return Set.copyOf(this.members.values());
    }

    @Override
    public Optional<FactionMember> getLeader()
    {
        return Optional.ofNullable(this.leader)
                .map(this.members::get);
    }

    @Override
    public Instant getLastOnline()
    {
        return this.lastOnline;
    }

    @Override
    public Instant getCreatedDate()
    {
        return this.lastOnline;
    }

    @Override
    public Set<FactionPermission> getRelationPermissions(RelationType relationType)
    {
        return Collections.unmodifiableSet(this.relationPermissions.getOrDefault(relationType, Set.of()));
    }

    @Override
    public Set<Rank> getPlayerRanks(final UUID playerUUID)
    {
        Set<Rank> playerRanks = getMembers().stream()
                .filter(factionMember -> playerUUID.equals(factionMember.getUniqueId()))
                .map(FactionMember::getRankNames)
                .flatMap(Collection::stream)
                .map(rankName -> getRank(rankName).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (Objects.equals(this.leader, playerUUID))
        {
            playerRanks.add(getLeaderRank());
        }

        if (playerRanks.isEmpty())
        {
            // Player should always have at least one rank, because we need to take permissions from somewhere :)
            playerRanks.add(getDefaultRank());
        }

        return playerRanks;
    }

    @Override
    public RelationType getRelationTo(Faction faction)
    {
        if (this.name.equalsIgnoreCase(faction.getName()))
            return RelationType.SAME_FACTION;
        if (this.alliances.contains(faction.getName().toLowerCase()))
            return RelationType.ALLIANCE;
        else if (this.truces.contains(faction.getName().toLowerCase()))
            return RelationType.TRUCE;
        else if (this.enemies.contains(faction.getName().toLowerCase()))
            return RelationType.ENEMY;
        else return RelationType.NONE;
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
    public boolean getProtectionFlagValue(ProtectionFlagType type)
    {
        return this.protectionFlags.getValueForFlag(type);
    }

    @Override
    public Set<ProtectionFlag> getProtectionFlags()
    {
        return this.protectionFlags.getProtectionFlags();
    }

    @Override
    public boolean containsPlayer(final UUID playerUUID)
    {
        if (Objects.equals(this.leader, playerUUID))
            return true;
        return this.members.containsKey(playerUUID);
    }

    @Override
    public Faction.Builder toBuilder()
    {
        final Faction.Builder factionBuilder = new BuilderImpl();
        factionBuilder.name(this.name);
        factionBuilder.tag(this.tag);
        factionBuilder.description(this.description);
        factionBuilder.messageOfTheDay(this.messageOfTheDay);
        factionBuilder.leader(this.leader);
        factionBuilder.members(new HashSet<>(this.members.values()));
        factionBuilder.alliances(this.alliances);
        factionBuilder.enemies(this.enemies);
        factionBuilder.claims(this.claims);
        factionBuilder.lastOnline(this.lastOnline);
        factionBuilder.createdDate(this.createdDate);
        factionBuilder.home(this.home);
        factionBuilder.chest(this.chest);
        factionBuilder.isPublic(this.isPublic);
        factionBuilder.protectionFlags(this.getProtectionFlags());
        factionBuilder.ranks(this.getRanks());
        factionBuilder.alliancePermissions(this.getRelationPermissions(RelationType.ALLIANCE));
        factionBuilder.trucePermissions(this.getRelationPermissions(RelationType.TRUCE));

        return factionBuilder;
    }

    public static Faction.Builder builder(final String name, final TextComponent tag)
    {
        return new BuilderImpl(name, tag);
    }

    @Override
    public int compareTo(final Faction object)
    {
        return this.name.compareTo(object.getName());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionImpl faction = (FactionImpl) o;
        return isPublic == faction.isPublic && Objects.equals(name, faction.name) && Objects.equals(tag, faction.tag) && Objects.equals(description, faction.description) && Objects.equals(messageOfTheDay, faction.messageOfTheDay) && Objects.equals(members, faction.members) && Objects.equals(truces, faction.truces) && Objects.equals(alliances, faction.alliances) && Objects.equals(enemies, faction.enemies) && Objects.equals(leader, faction.leader) && Objects.equals(claims, faction.claims) && Objects.equals(home, faction.home) && Objects.equals(lastOnline, faction.lastOnline) && Objects.equals(createdDate, faction.createdDate) && Objects.equals(relationPermissions, faction.relationPermissions) && Objects.equals(ranks, faction.ranks) && Objects.equals(chest, faction.chest) && Objects.equals(protectionFlags, faction.protectionFlags);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, tag, description, messageOfTheDay, members, truces, alliances, enemies, leader, claims, home, lastOnline, createdDate, isPublic, relationPermissions, ranks, chest, protectionFlags);
    }

    private Optional<Rank> getRankByName(String rankName)
    {
        return this.ranks.stream()
                .filter(rank -> rank.getName().equalsIgnoreCase(rankName))
                .findFirst();
    }

    @Override
    public String toString()
    {
        return "FactionImpl{" +
                "name='" + name + '\'' +
                ", tag=" + tag +
                ", description='" + description + '\'' +
                ", messageOfTheDay='" + messageOfTheDay + '\'' +
                ", members=" + members +
                ", truces=" + truces +
                ", alliances=" + alliances +
                ", enemies=" + enemies +
                ", leader=" + leader +
                ", claims=" + claims +
                ", home=" + home +
                ", lastOnline=" + lastOnline +
                ", createdDate=" + createdDate +
                ", isPublic=" + isPublic +
                ", relationPermissions=" + relationPermissions +
                ", ranks=" + ranks +
                ", chest=" + chest +
                ", protectionFlags=" + protectionFlags +
                '}';
    }

    //Builder
    public static final class BuilderImpl implements Faction.Builder
    {
        private String name;
        private TextComponent tag;
        private String description;
        private String messageOfTheDay;
        private UUID leader;
        private Set<FactionMember> members;
        private Set<String> truces;
        private Set<String> alliances;
        private Set<String> enemies;
        private Set<Claim> claims;
        private FactionHome home;
        private Instant lastOnline;
        private Instant createdDate;
        private List<Rank> ranks;
        private Set<FactionPermission> alliancePermissions;
        private Set<FactionPermission> trucePermissions;
        private FactionChest chest;
        private Set<ProtectionFlag> protectionFlags;
        private boolean isPublic;

        private BuilderImpl()
        {
            this.description = "";
            this.messageOfTheDay = "";
            this.members = new HashSet<>();
            this.truces = new HashSet<>();
            this.alliances = new HashSet<>();
            this.enemies = new HashSet<>();
            this.claims = new HashSet<>();
            this.protectionFlags = new HashSet<>();
            this.ranks = null;
            this.home = null;
            this.isPublic = false;
            this.alliancePermissions = null;
            this.trucePermissions = null;
        }

        public BuilderImpl(final String name, final TextComponent tag)
        {
            this();
            this.name = name;
            this.tag = tag;
            this.chest = new FactionChestImpl(this.name);
        }

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder tag(final TextComponent tag)
        {
            this.tag = tag;
            return this;
        }

        public Builder description(final String description)
        {
            this.description = description;
            return this;
        }

        public Builder messageOfTheDay(final String messageOfTheDay)
        {
            this.messageOfTheDay = messageOfTheDay;
            return this;
        }

        public Builder leader(final UUID leaderUUID)
        {
            this.leader = leaderUUID;
            this.members.add(new FactionMemberImpl(leaderUUID, Set.of(RankManagerImpl.LEADER_RANK_NAME)));
            return this;
        }

        public Builder members(final Set<FactionMember> members)
        {
            this.members = members;
            return this;
        }

        public Builder truces(final Set<String> truces)
        {
            this.truces = truces;
            return this;
        }

        public Builder alliances(final Set<String> alliances)
        {
            this.alliances = alliances;
            return this;
        }

        public Builder enemies(final Set<String> enemies)
        {
            this.enemies = enemies;
            return this;
        }

        public Builder claims(final Set<Claim> claims)
        {
            this.claims = claims;
            return this;
        }

        public Builder home(final FactionHome home)
        {
            this.home = home;
            return this;
        }

        public Builder lastOnline(final Instant lastOnline)
        {
            this.lastOnline = lastOnline;
            return this;
        }

        public Builder createdDate(final Instant createdDate)
        {
            this.createdDate = createdDate;
            return this;
        }

        @Override
        public Builder ranks(final List<Rank> ranks)
        {
            this.ranks = ranks;
            return this;
        }

        @Override
        public Builder alliancePermissions(Set<FactionPermission> permissions)
        {
            this.alliancePermissions = permissions;
            return this;
        }

        @Override
        public Builder trucePermissions(Set<FactionPermission> permissions)
        {
            this.trucePermissions = permissions;
            return this;
        }

        public Builder chest(final FactionChest chest)
        {
            this.chest = chest;
            return this;
        }

        public Builder isPublic(final boolean isPublic)
        {
            this.isPublic = isPublic;
            return this;
        }

        public Builder protectionFlags(Set<ProtectionFlag> protectionFlags)
        {
            this.protectionFlags = protectionFlags;
            return this;
        }

        public Faction build()
        {
            if(this.name == null || this.tag == null)
                throw new IllegalStateException("Couldn't build Faction object! Faction must have a name and a tag!");

            if(this.lastOnline == null)
                this.lastOnline = Instant.now();
            if (this.createdDate == null)
                this.createdDate = Instant.now();
            if(this.ranks == null)
                this.ranks = new ArrayList<>();
            if (this.alliancePermissions == null)
                this.alliancePermissions = PermsManagerImpl.getDefaultAlliancePermissions();
            if (this.trucePermissions == null)
                this.trucePermissions = PermsManagerImpl.getDefaultTrucePermissions();
            if(this.chest == null)
                this.chest = new FactionChestImpl(this.name);

            return new FactionImpl(this);
        }
    }
}
