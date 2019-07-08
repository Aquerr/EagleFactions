package io.github.aquerr.eaglefactions.api.logic;

import org.spongepowered.api.entity.living.player.Player;

public interface PVPLogger
{
    boolean isActive();

    int getBlockTime();

    boolean shouldBlockCommand(Player player, String usedCommand);

    void addOrUpdatePlayer(Player player);

    boolean isPlayerBlocked(Player player);

    void removePlayer(Player player);

    int getPlayerBlockTime(Player player);
}
