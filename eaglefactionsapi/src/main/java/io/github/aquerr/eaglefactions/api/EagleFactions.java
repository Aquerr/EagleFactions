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
    void printInfo(String message);

    IConfiguration getConfiguration();
    Path getConfigDir();
    URL getResource(String fileName);

    IPlayerManager getPlayerManager();
    IFlagManager getFlagManager();
    IPowerManager getPowerManager();
    IProtectionManager getProtectionManager();
    PVPLogger getPVPLogger();

    FactionLogic getFactionLogic();
    AttackLogic getAttackLogic();

    StorageManager getStorageManager();
}
