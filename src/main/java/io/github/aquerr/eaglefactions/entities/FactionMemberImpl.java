package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.FactionMember;

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
}
