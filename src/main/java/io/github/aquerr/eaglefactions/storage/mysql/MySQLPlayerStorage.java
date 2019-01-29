package io.github.aquerr.eaglefactions.storage.mysql;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.storage.IPlayerStorage;

import java.util.Set;
import java.util.UUID;

public class MySQLPlayerStorage implements IPlayerStorage
{
    public MySQLPlayerStorage(EagleFactions eagleFactions)
    {

    }

    @Override
    public boolean checkIfPlayerExists(UUID playerUUID, String playerName)
    {
        return false;
    }

    @Override
    public boolean addPlayer(UUID playerUUID, String playerName, float startingPower, float maxPower)
    {
        return false;
    }

    @Override
    public boolean setDeathInWarzone(UUID playerUUID, boolean didDieInWarZone)
    {
        return false;
    }

    @Override
    public boolean getLastDeathInWarzone(UUID playerUUID)
    {
        return false;
    }

    @Override
    public float getPlayerPower(UUID playerUUID)
    {
        return 0;
    }

    @Override
    public boolean setPlayerPower(UUID playerUUID, float power)
    {
        return false;
    }

    @Override
    public float getPlayerMaxPower(UUID playerUUID)
    {
        return 0;
    }

    @Override
    public boolean setPlayerMaxPower(UUID playerUUID, float maxpower)
    {
        return false;
    }

    @Override
    public Set<String> getServerPlayerNames()
    {
        return null;
    }

    @Override
    public Set<IFactionPlayer> getServerPlayers()
    {
        return null;
    }

    @Override
    public String getPlayerName(UUID playerUUID)
    {
        return null;
    }

    @Override
    public boolean updatePlayerName(UUID playerUUID, String playerName)
    {
        return false;
    }
}
