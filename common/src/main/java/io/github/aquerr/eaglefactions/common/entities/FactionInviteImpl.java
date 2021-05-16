package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;

import java.util.Objects;
import java.util.UUID;

public class FactionInviteImpl implements FactionInvite
{
    private final String factionName;
    private final UUID playerUUID;

    public FactionInviteImpl(String factionName, UUID playerUUID)
    {
        this.factionName = factionName;
        this.playerUUID = playerUUID;
    }

    @Override
    public void accept()
    {
        EagleFactionsPlugin.getPlugin().getInvitationManager().acceptInvitation(this);
    }

    @Override
    public String getSenderFaction()
    {
        return this.factionName;
    }

    @Override
    public UUID getInvitedPlayerUniqueId()
    {
        return this.playerUUID;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionInviteImpl that = (FactionInviteImpl) o;
        return factionName.equals(that.factionName) && playerUUID.equals(that.playerUUID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(factionName, playerUUID);
    }

    @Override
    public String toString()
    {
        return "FactionInviteImpl{" +
                "factionName='" + factionName + '\'' +
                ", playerUUID=" + playerUUID +
                '}';
    }
}
