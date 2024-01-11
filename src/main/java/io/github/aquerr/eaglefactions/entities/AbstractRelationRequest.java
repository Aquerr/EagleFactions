package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.AcceptableInvite;
import io.github.aquerr.eaglefactions.api.entities.Faction;

public abstract class AbstractRelationRequest implements AcceptableInvite<Faction, Faction>
{
    private final Faction sender;
    private final Faction invited;

    protected AbstractRelationRequest(Faction sender, Faction invited)
    {
        this.sender = sender;
        this.invited = invited;
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
}
