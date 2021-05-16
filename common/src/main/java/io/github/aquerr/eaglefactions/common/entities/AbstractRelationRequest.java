package io.github.aquerr.eaglefactions.common.entities;

import java.util.Objects;

public abstract class AbstractRelationRequest
{
    private final String senderFaction;
    private final String invitedFaction;

    protected AbstractRelationRequest(String senderFaction, String invitedFaction)
    {
        this.senderFaction = senderFaction;
        this.invitedFaction = invitedFaction;
    }

    public String getSenderFaction()
    {
        return senderFaction;
    }

    public String getInvitedFaction()
    {
        return invitedFaction;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRelationRequest that = (AbstractRelationRequest) o;
        return senderFaction.equals(that.senderFaction) && invitedFaction.equals(that.invitedFaction);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(senderFaction, invitedFaction);
    }
}
