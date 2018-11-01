package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.entities.IFactionPlayer;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public interface IPlayerStorage
{
    boolean checkIfPlayerExists(UUID playerUUID, String playerName);

    boolean addPlayer(UUID playerUUID, String playerName, BigDecimal startingPower, BigDecimal maxPower);

    boolean setDeathInWarzone(UUID playerUUID, boolean didDieInWarZone);

    boolean getLastDeathInWarzone(UUID playerUUID);

    BigDecimal getPlayerPower(UUID playerUUID);

    boolean setPlayerPower(UUID playerUUID, BigDecimal power);

    BigDecimal getPlayerMaxPower(UUID playerUUID);

    boolean setPlayerMaxPower(UUID playerUUID, BigDecimal maxpower);

    Set<String> getServerPlayerNames();

    Set<IFactionPlayer> getServerPlayers();

    String getPlayerName(UUID playerUUID);

    void updatePlayerName(UUID playerUUID, String playerName);
}
