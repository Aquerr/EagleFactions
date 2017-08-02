package io.github.aquerr.eaglefactions.entities;

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
}
