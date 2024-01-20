package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.AcceptableInvite;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.RelationType;

public abstract class AbstractRelationRequest implements AcceptableInvite<Faction, Faction>
{
    private final Faction sender;
    private final Faction invited;
    private final RelationType relationType;

    protected AbstractRelationRequest(Faction sender, Faction invited, RelationType relationType)
    {
        this.sender = sender;
        this.invited = invited;
        this.relationType = relationType;
    }

    @Override
    public Faction getInvited()
    {
        return invited;
    }

    @Override
    public Faction getSender()
    {
        return sender;
    }

    public RelationType getRelationType()
    {
        return relationType;
    }
}
