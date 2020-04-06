package io.github.aquerr.eaglefactions.common.storage;

import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PlayerStorage
{
    boolean checkIfPlayerExists(UUID playerUUID, String playerName);

    boolean addPlayer(UUID playerUUID, String playerName, float startingPower, float maxPower);

    boolean addPlayers(List<FactionPlayer> players);

    boolean setDeathInWarzone(UUID playerUUID, boolean didDieInWarZone);

    boolean getLastDeathInWarzone(UUID playerUUID);

    float getPlayerPower(UUID playerUUID);

    boolean setPlayerPower(UUID playerUUID, float power);

    float getPlayerMaxPower(UUID playerUUID);

    boolean setPlayerMaxPower(UUID playerUUID, float maxpower);

    Set<String> getServerPlayerNames();

    Set<FactionPlayer> getServerPlayers();

    String getPlayerName(UUID playerUUID);

    boolean updatePlayerName(UUID playerUUID, String playerName);

    void deletePlayers();
}
