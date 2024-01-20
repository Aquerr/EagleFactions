package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.Rank;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class RankImpl implements Rank
{
    private String name; // Unique
    private String displayName; // Nullable
    private int ladderPosition;
    private Set<FactionPermission> permissions;
    private boolean displayInChat;

    private RankImpl(BuilderImpl builder)
    {
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.ladderPosition = builder.ladderPosition;
        this.permissions = builder.permissions;
        this.displayInChat = builder.displayInChat;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getDisplayName()
    {
        return Optional.ofNullable(displayName).orElse(name);
    }

    @Override
    public int getLadderPosition()
    {
        return ladderPosition;
    }

    @Override
    public Set<FactionPermission> getPermissions()
    {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean canDisplayInChat()
    {
        return this.displayInChat;
    }

    public static Builder builder()
    {
        return new RankImpl.BuilderImpl();
    }

    @Override
    public Builder toBuilder()
    {
        final Rank.Builder rankBuilder = new RankImpl.BuilderImpl();
        rankBuilder.name(this.name);
        rankBuilder.displayName(this.displayName);
        rankBuilder.ladderPosition(this.ladderPosition);
        rankBuilder.displayInChat(this.displayInChat);
        rankBuilder.permissions(this.permissions);
        return rankBuilder;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankImpl rank = (RankImpl) o;
        return ladderPosition == rank.ladderPosition && displayInChat == rank.displayInChat && Objects.equals(name, rank.name) && Objects.equals(displayName, rank.displayName) && Objects.equals(permissions, rank.permissions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, displayName, ladderPosition, permissions, displayInChat);
    }

    @Override
    public String toString()
    {
        return "RankImpl{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", ladderPosition=" + ladderPosition +
                ", permissions=" + permissions +
                ", displayInChat=" + displayInChat +
                '}';
    }

    public static class BuilderImpl implements Builder
    {
        private String name;
        private String displayName;
        private int ladderPosition;
        private Set<FactionPermission> permissions = new HashSet<>();
        private boolean displayInChat;

        @Override
        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        @Override
        public Builder displayName(String displayName)
        {
            this.displayName = displayName;
            return this;
        }

        @Override
        public Builder ladderPosition(int ladderPosition)
        {
            this.ladderPosition = ladderPosition;
            return this;
        }

        @Override
        public Builder permissions(Set<FactionPermission> permissions)
        {
            this.permissions = permissions;
            return this;
        }

        @Override
        public Builder permission(FactionPermission permission)
        {
            this.permissions.add(permission);
            return this;
        }

        @Override
        public Builder displayInChat(boolean displayInChat)
        {
            this.displayInChat = displayInChat;
            return this;
        }

        @Override
        public Rank build()
        {
            return new RankImpl(this);
        }
    }
}

