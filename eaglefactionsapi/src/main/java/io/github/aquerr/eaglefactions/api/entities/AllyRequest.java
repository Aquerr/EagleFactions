package io.github.aquerr.eaglefactions.api.entities;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class AllyRequest
{
    private String factionName;
    private String invitedFaction;

    public AllyRequest(String factionName, String invitedFaction)
    {
        this.factionName = factionName;
        this.invitedFaction = invitedFaction;
    }

    public String getFactionName()
    {
        return factionName;
    }

    public String getInvitedFaction()
    {
        return invitedFaction;
    }

    @Override
    public boolean equals (Object allyInvite)
    {
        if(!(allyInvite instanceof AllyRequest))
        {
            return false;
        }
        if(allyInvite == this)
        {
            return true;
        }
        return this.factionName.equals(((AllyRequest) allyInvite).factionName) && this.invitedFaction.equals(((AllyRequest) allyInvite).invitedFaction);
    }

    @Override
    public int hashCode()
    {
        return factionName.hashCode();
    }
}
