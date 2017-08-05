package io.github.aquerr.eaglefactions.entities;

import com.google.common.hash.HashCode;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class AllyInvite
{
    public AllyInvite(String factionName, String invitedFaction)
    {
        this.FactionName = factionName;
        this.InvitedFaction = invitedFaction;
    }

    public String FactionName;
    public String InvitedFaction;


    @Override
    public boolean equals (Object allyInvite)
    {
       // boolean isEqual = false;
//
       // if(allyInvite instanceof AllyInvite)
       // {
       //     if((this.FactionName == ((AllyInvite) allyInvite).FactionName) && this.InvitedFaction == ((AllyInvite) allyInvite).InvitedFaction)
       //     {
       //         isEqual = true;
       //         return isEqual;
       //     }
       // }
//
       // return isEqual;

        if(!(allyInvite instanceof AllyInvite))
        {
            return false;
        }
        if(allyInvite == this)
        {
            return true;
        }
        return this.FactionName.equals(((AllyInvite) allyInvite).FactionName) && this.InvitedFaction.equals(((AllyInvite) allyInvite).InvitedFaction);
    }

    @Override
    public int hashCode()
    {
        return FactionName.length();
    }
}
