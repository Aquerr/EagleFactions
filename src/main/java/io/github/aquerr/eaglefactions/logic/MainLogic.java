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

        double incrementPower = powerIncrementNode.getDouble();

        return incrementPower;
    }

    public static double getPowerDecrement()
    {
        ConfigurationNode powerDecrementNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "decrement");

        double decrementPower = powerDecrementNode.getDouble();

        return decrementPower;
    }

    public static double getKillAward()
    {
        ConfigurationNode killAwardNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "killaward");

        double killAward = killAwardNode.getDouble();

        return killAward;
    }
}
