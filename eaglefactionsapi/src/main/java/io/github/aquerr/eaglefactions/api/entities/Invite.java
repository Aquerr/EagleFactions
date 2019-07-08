package io.github.aquerr.eaglefactions.api.entities;

import java.util.UUID;

public class Invite
{
    private String _factionName;
    private UUID _playerUUID;

    public Invite(String factionName, UUID playerUUID)
    {
        this._factionName = factionName;
        this._playerUUID = playerUUID;
    }

    public String getFactionName()
    {
        return _factionName;
    }

    public UUID getPlayerUUID()
    {
        return _playerUUID;
    }

    @Override
    public boolean equals (Object allyInvite)
    {
        if(!(allyInvite instanceof Invite))
        {
            return false;
        }
        if(allyInvite == this)
        {
            return true;
        }
        return this._factionName.equals(((Invite) allyInvite)._factionName) && this._playerUUID.equals(((Invite) allyInvite)._playerUUID);
    }

    @Override
    public int hashCode()
    {
        return _factionName.length();
    }
}
