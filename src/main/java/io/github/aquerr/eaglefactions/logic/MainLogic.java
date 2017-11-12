package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.config.ConfigAccess;
import io.github.aquerr.eaglefactions.config.IConfig;
import io.github.aquerr.eaglefactions.config.MainConfig;
import ninja.leaping.configurate.ConfigurationNode;

import java.math.BigDecimal;

public class MainLogic
{
    private static IConfig mainConfig = MainConfig.getConfig();

    public static boolean getAllianceFriendlyFire()
    {
        ConfigurationNode friendlyFireNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "friendlyFire", "alliance");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static BigDecimal getGlobalMaxPower()
    {
        ConfigurationNode maxPowerNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "maxpower");

        BigDecimal maxPower = new BigDecimal(maxPowerNode.getString());

        return maxPower;
    }

    public static BigDecimal getStartingPower()
    {
        ConfigurationNode startingPowerNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "startpower");

        BigDecimal startPower = new BigDecimal(startingPowerNode.getString());

        return startPower;
    }

    public static BigDecimal getPowerIncrement()
    {
        ConfigurationNode powerIncrementNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "increment");

        BigDecimal incrementPower = new BigDecimal(powerIncrementNode.getString());

        return incrementPower;
    }

    public static BigDecimal getPowerDecrement()
    {
        ConfigurationNode powerDecrementNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "decrement");

        BigDecimal decrementPower = new BigDecimal(powerDecrementNode.getString());

        return decrementPower;
    }

    public static BigDecimal getKillAward()
    {
        ConfigurationNode killAwardNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "killaward");

        BigDecimal killAward = new BigDecimal(killAwardNode.getString());

        return killAward;
    }

    public static BigDecimal getPunishment()
    {
        ConfigurationNode punishmentNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "power", "punishment");

        BigDecimal punishment = new BigDecimal(punishmentNode.getString());

        return punishment;
    }

    public static int getMaxNameLength()
    {
        ConfigurationNode maxLengthNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "name", "maxlength");

        int maxLength = maxLengthNode.getInt();

        return maxLength;
    }

    public static int getMinNameLength()
    {
        ConfigurationNode minLengthNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "name", "minlength");

        int minLength = minLengthNode.getInt();

        return minLength;
    }

    public static int getMaxTagLength()
    {
        ConfigurationNode maxLengthNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "tag", "maxlength");

        int maxLength = maxLengthNode.getInt();

        return maxLength;
    }

    public static int getMinTagLength()
    {
        ConfigurationNode minLengthNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "tag", "minlength");

        int minLength = minLengthNode.getInt();

        return minLength;
    }

    public static boolean getMobSpawning()
    {
        ConfigurationNode mobSpawningNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "spawn", "mobs");

        boolean mobSpawning = mobSpawningNode.getBoolean();

        return mobSpawning;
    }

    public static boolean getBlockEnteringFactions()
    {
        ConfigurationNode mobSpawningNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "gameplay", "blockEnteringFactions");

        boolean mobSpawning = mobSpawningNode.getBoolean();

        return mobSpawning;
    }

    public static boolean requireConnectedClaims()
    {
        ConfigurationNode requireConnectedClaimsNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "gameplay", "connectedClaims");

        boolean requireConnectedClaims = requireConnectedClaimsNode.getBoolean();

        return requireConnectedClaims;
    }

    public static boolean shouldBlockSafeZoneFromWarZone()
    {
        ConfigurationNode blockSafeZoneFromWarZoneNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "gameplay", "blockSafeZoneWhileInWarZone");

        boolean blockSafeZoneFromWarZone = blockSafeZoneFromWarZoneNode.getBoolean();

        return blockSafeZoneFromWarZone;
    }

    public static boolean isPlayerLimit()
    {
        ConfigurationNode isPlayerLimitNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "playerlimit", "playerlimit");

        boolean playerLimit = isPlayerLimitNode.getBoolean();

        return playerLimit;
    }

    public static int getPlayerLimit()
    {
        ConfigurationNode limitNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "playerlimit", "limit");

        int limit = limitNode.getInt();

        return limit;
    }

    public static int getAttackTime()
    {
        ConfigurationNode attackTimeNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "gameplay", "attacktime");

        int attackTime = attackTimeNode.getInt();

        return attackTime;
    }
}
