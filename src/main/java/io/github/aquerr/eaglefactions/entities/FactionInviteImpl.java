package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;

import java.util.Objects;

public class FactionInviteImpl implements FactionInvite
{
    private final Faction sender;
    private final FactionPlayer invited;

    public FactionInviteImpl(Faction sender, FactionPlayer invited)
    {
        this.sender = sender;
        this.invited = invited;
    }

    @Override
    public void accept()
    {
        EagleFactionsPlugin.getPlugin().getInvitationManager().acceptInvitation(this);
    }

    @Override
    public Faction getSender()
    {
        return sender;
    }

    @Override
    public FactionPlayer getInvited()
    {
        return invited;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionInviteImpl that = (FactionInviteImpl) o;
        return Objects.equals(sender, that.sender) && Objects.equals(invited, that.invited);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sender, invited);
    }

    @Override
    public String toString()
    {
        return "FactionInviteImpl{" +
                "sender=" + sender +
                ", invited=" + invited +
                '}';
    }
}
