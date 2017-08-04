package io.github.aquerr.eaglefactions.entities;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class AllayInvite
{
    public AllayInvite(String factionName, String invitedFaction)
    {
        this.FactionName = factionName;
        this.InvitedFaction = invitedFaction;
    }

    public String FactionName;
    public String InvitedFaction;

}
