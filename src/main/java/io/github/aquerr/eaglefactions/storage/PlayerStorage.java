package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PlayerStorage
{
    @Nullable FactionPlayer getPlayer(UUID playerUUID);

    boolean savePlayer(FactionPlayer player);

    boolean savePlayers(List<FactionPlayer> players);

    Set<String> getServerPlayerNames();

    Set<FactionPlayer> getServerPlayers();

    void deletePlayers();
}
