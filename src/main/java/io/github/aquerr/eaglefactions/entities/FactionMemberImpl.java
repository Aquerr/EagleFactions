package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.FactionMember;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class FactionMemberImpl implements FactionMember
{
    private final UUID uniqueId;
    private final Set<String> rankNames;

    public FactionMemberImpl(UUID uniqueId, Set<String> rankNames)
    {
        this.uniqueId = uniqueId;
        this.rankNames = rankNames;
    }

    public UUID getUniqueId()
    {
        return uniqueId;
    }

    public Set<String> getRankNames()
    {
        return rankNames;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionMemberImpl that = (FactionMemberImpl) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId);
    }

    @Override
    public String toString()
    {
        return "FactionMemberImpl{" +
                "uniqueId=" + uniqueId +
                ", rankNames=" + rankNames +
                '}';
    }
}
