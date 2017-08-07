package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.config.ConfigAccess;
import io.github.aquerr.eaglefactions.config.IConfig;
import io.github.aquerr.eaglefactions.config.MainConfig;
import ninja.leaping.configurate.ConfigurationNode;

public class MainLogic
{
    private static IConfig mainConfig = MainConfig.getConfig();

    public static boolean getAllianceFriendlyFire()
    {
        ConfigurationNode friendlyFireNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "friendlyFire", "alliance");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static int getPlayerMaxPower()
    {
        ConfigurationNode maxPowerNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "maxpower");

        int maxPower = maxPowerNode.getInt();

        return maxPower;
    }

    public static int getStartingPower()
    {
        ConfigurationNode startingPowerNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "startpower");

        int startPower = startingPowerNode.getInt();

        return startPower;
    }
}
