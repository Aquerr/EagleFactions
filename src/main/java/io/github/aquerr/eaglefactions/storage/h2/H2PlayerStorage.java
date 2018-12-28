package io.github.aquerr.eaglefactions.storage.h2;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.storage.IPlayerStorage;

import java.util.Set;
import java.util.UUID;

public class H2PlayerStorage implements IPlayerStorage
{
    public H2PlayerStorage(final EagleFactions eagleFactions)
    {

    }

    @Override
    public boolean checkIfPlayerExists(final UUID playerUUID, final String playerName)
    {
        return false;
    }

    @Override
    public boolean addPlayer(final UUID playerUUID, final String playerName, final float startingPower, final float maxPower)
    {
        return false;
    }

    @Override
    public boolean setDeathInWarzone(final UUID playerUUID, final boolean didDieInWarZone)
    {
        return false;
    }

    @Override
    public boolean getLastDeathInWarzone(final UUID playerUUID)
    {
        return false;
    }

    @Override
    public float getPlayerPower(final UUID playerUUID)
    {
        return 0;
    }

    @Override
    public boolean setPlayerPower(final UUID playerUUID, final float power)
    {
        return false;
    }

    @Override
    public float getPlayerMaxPower(final UUID playerUUID)
    {
        return 0;
    }

    @Override
    public boolean setPlayerMaxPower(final UUID playerUUID, final float maxpower)
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
    public String getPlayerName(final UUID playerUUID)
    {
        return null;
    }

    @Override
    public void updatePlayerName(final UUID playerUUID, final String playerName)
    {

    }
}
