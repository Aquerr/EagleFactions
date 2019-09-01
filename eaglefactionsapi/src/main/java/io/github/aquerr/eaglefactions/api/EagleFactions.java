package io.github.aquerr.eaglefactions.api;

import io.github.aquerr.eaglefactions.api.config.IConfiguration;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.managers.IFlagManager;
import io.github.aquerr.eaglefactions.api.managers.IPlayerManager;
import io.github.aquerr.eaglefactions.api.managers.IPowerManager;
import io.github.aquerr.eaglefactions.api.managers.IProtectionManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;

import java.net.URL;
import java.nio.file.Path;

public interface EagleFactions
{
    /**
     * Prints info from Eagle Factions in the server chat channel.
     * @param message that will be printed.
     */
    void printInfo(final String message);

    /**
     * Gets Eagle Factions configuration object. Used to access any EF config related settings.
     * @return instance of {@link IConfiguration}
     */
    IConfiguration getConfiguration();

    /**
     * Get Eagle Factions config path.
     * @return Path to the config folder.
     */
    Path getConfigDir();

    /**
     * Gets Eagle Factions resource file.
     * @param fileName to get from resources
     * @return <tt>URL</tt> object of that file or <tt>null</tt> if file could not be found.
     */
    URL getResource(final String fileName);

    /**
     * @return instance of {@link IPlayerManager}
     */
    IPlayerManager getPlayerManager();

    /**
     * @return instance of {@link IFlagManager}
     */
    IFlagManager getFlagManager();

    /**
     * @return instance of {@link IPowerManager}
     */
    IPowerManager getPowerManager();

    /**
     * @return instance of IProtectionManager
     */
    IProtectionManager getProtectionManager();

    /**
     * @return instance of {@link PVPLogger}
     */
    PVPLogger getPVPLogger();

    /**
     * @return instance of {@link FactionLogic}
     */
    FactionLogic getFactionLogic();

    /**
     * @return instance of {@link AttackLogic}
     */
    AttackLogic getAttackLogic();

    /**
     * @return instance of {@link StorageManager}
     */
    StorageManager getStorageManager();
}
