package io.github.aquerr.eaglefactions.api.storage;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.IFactionPlayer;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public interface StorageManager {

    void addOrUpdateFaction(Faction faction);

    boolean deleteFaction(String factionName);

    @Nullable
    Faction getFaction(String factionName);

    void reloadStorage();

    boolean checkIfPlayerExists(UUID playerUUID, String playerName);

    boolean addPlayer(UUID playerUUID, String playerName, float startingPower, float globalMaxPower);

    boolean setDeathInWarzone(UUID playerUUID, boolean didDieInWarZone);

    boolean getLastDeathInWarzone(UUID playerUUID);

    float getPlayerPower(UUID playerUUID);

    boolean setPlayerPower(UUID playerUUID, float power);

    float getPlayerMaxPower(UUID playerUUID);

    boolean setPlayerMaxPower(UUID playerUUID, float maxpower);

    Set<String> getServerPlayerNames();

    Set<IFactionPlayer> getServerPlayers();

    String getPlayerName(UUID playerUUID);

    boolean updatePlayerName(UUID playerUUID, String playerName);
}
