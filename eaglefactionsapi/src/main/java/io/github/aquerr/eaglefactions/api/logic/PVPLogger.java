package io.github.aquerr.eaglefactions.api.logic;

import org.spongepowered.api.entity.living.player.Player;

public interface PVPLogger
{
    /**
     * Checks if {@link PVPLogger} is active or not.
     * @return <tt>true</tt> if PVPLogger is active, <tt>false</tt> if not
     */
    boolean isActive();

    /**
     * Gets the global blocking-time a player is being blocked after being hit by another player.
     * This number comes directly from the config file.
     * @return the time a player should be blocked.
     */
    int getBlockTime();

    /**
     * Checks if a command should be blocked by PVPLogger if a player is blocked.
     * @param player the player that should be checked against PVPLogger.
     * @param command the command that should be checked by PVPLogger.
     * @return <tt>true</tt> if command player is currently blocked and the given command should be blocked.
     * <tt>false</tt> if the player is not currently blocked or a command should not be blocked.
     */
    boolean shouldBlockCommand(final Player player, final String command);

    /**
     * Adds or updates player in PVPLogger.
     * This method adds the player to the PVPLogger if player does not already exist there. After that, blocking task is being started for the player.
     * If player already exists in PVPLogger then the player's blocking time should be reset (updated).
     * @param player the player that should be added/updated by PVPLogger.
     */
    void addOrUpdatePlayer(final Player player);

    /**
     * Checks if player is being blocked by PVPLogger.
     * @param player the player that should be checked.
     * @return <tt>true</tt> if player is being blocked, <tt>false</tt> if not.
     */
    boolean isPlayerBlocked(final Player player);

    /**
     * Removes player from the PVPLogger.
     * @param player the player that should be removed from blocking.
     *
     * This method uses {@link PVPLogger#isPlayerBlocked(Player)} internally to check if the player is being blocked first.
     */
    void removePlayer(final Player player);

    /**
     * Gets current block time for the player.
     * @param player player the block time should be get for.
     * @return block time for the given player or 0 if player is not being blocked.
     */
    int getPlayerBlockTime(final Player player);
}
