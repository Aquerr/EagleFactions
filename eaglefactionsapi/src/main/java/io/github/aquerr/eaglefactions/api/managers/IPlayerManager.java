package io.github.aquerr.eaglefactions.api.managers;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.IFactionPlayer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IPlayerManager
{
    boolean addPlayer(UUID playerUUID, String name);
    Optional<Player> getPlayer(UUID playerUUID);

    FactionMemberType getFactionMemberType(UUID playerUUID, Faction faction);
    float getPlayerPower(UUID playerUUID);
    float getPlayerMaxPower(UUID playerUUID);
    boolean setPlayerPower(UUID playerUUID, float power);
    boolean setPlayerMaxPower(UUID playerUUID, float power);

    boolean isPlayerOnline(UUID playerUUID);

    boolean lastDeathAtWarZone(UUID playerUUID);

    Set<String> getServerPlayerNames();

    void setDeathInWarZone(UUID playerUUID, boolean didDie);

    boolean checkIfPlayerExists(UUID playerUUID, String playerName);

    Set<IFactionPlayer> getServerPlayers();

    void updatePlayerName(UUID playerUUID, String playerName);

    Optional<String> getPlayerName(UUID playerUUID);


    IFactionPlayer convertToFactionPlayer(User user);
}
