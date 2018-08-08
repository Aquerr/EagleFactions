package io.github.aquerr.eaglefactions.entities;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class AllyInvite
{
    private String factionName;
    private String invitedFaction;

    public AllyInvite(String factionName, String invitedFaction)
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
        if(!(allyInvite instanceof AllyInvite))
        {
            return false;
        }
        if(allyInvite == this)
        {
            return true;
        }
        return this.factionName.equals(((AllyInvite) allyInvite).factionName) && this.invitedFaction.equals(((AllyInvite) allyInvite).invitedFaction);
    }

    @Override
    public int hashCode()
    {
        return factionName.length();
    }
}
