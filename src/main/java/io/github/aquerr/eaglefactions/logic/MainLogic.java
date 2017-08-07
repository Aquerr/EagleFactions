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

    public static double getGlobalMaxPower()
    {
        ConfigurationNode maxPowerNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "maxpower");

        double maxPower = maxPowerNode.getInt();

        return maxPower;
    }

    public static double getStartingPower()
    {
        ConfigurationNode startingPowerNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "startpower");

        double startPower = startingPowerNode.getInt();

        return startPower;
    }

    public static double getPowerIncrement()
    {
        ConfigurationNode powerIncrementNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "increment");

        double startPower = powerIncrementNode.getDouble();

        return startPower;
    }
}
