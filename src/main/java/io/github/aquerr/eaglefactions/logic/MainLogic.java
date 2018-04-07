package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.config.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class MainLogic
{
    private static Configuration _configuration;

    public static void setup(Configuration configuration)
    {
        _configuration = configuration;
    }

    public static boolean isFactionFriendlyFire()
    {
        return _configuration.getBoolean("friendlyfire-faction");
    }

    public static boolean isAllianceFriendlyFire()
    {
        return _configuration.getBoolean("friendlyfire-alliance");
    }

    public static BigDecimal getGlobalMaxPower()
    {
        return new BigDecimal(_configuration.getString("power", "max-power"));
    }

    public static BigDecimal getStartingPower()
    {
        return new BigDecimal(_configuration.getString("power", "start-power"));
    }

    public static BigDecimal getPowerIncrement()
    {
        return new BigDecimal(_configuration.getString("power", "increment"));
    }

    public static BigDecimal getPowerDecrement()
    {
        return new BigDecimal(_configuration.getString("power", "decrement"));
    }

    public static BigDecimal getKillAward()
    {
        return new BigDecimal(_configuration.getString("power", "killaward"));
    }

    public static BigDecimal getPenalty()
    {
        return new BigDecimal(_configuration.getString("power", "penalty"));
    }

    public static int getMaxNameLength()
    {
        return _configuration.getInt("name", "max-length");
    }

    public static int getMinNameLength()
    {
        return _configuration.getInt("name", "min-length");
    }

    public static int getMaxTagLength()
    {
        return _configuration.getInt("tag", "max-length");
    }

    public static int getMinTagLength()
    {
        return _configuration.getInt("tag", "min-length");
    }

    public static boolean getMobSpawning()
    {
        return _configuration.getBoolean("spawn", "mobs");
    }

    public static boolean getBlockEnteringFactions()
    {
        return _configuration.getBoolean("block-entering-faction-while-offline");
    }

    public static boolean requireConnectedClaims()
    {
        return _configuration.getBoolean("connected-claims");
    }

    public static boolean shouldBlockSafeZoneFromWarZone()
    {
        return _configuration.getBoolean("block-safezone-from-warzone");
    }

    public static boolean isPlayerLimit()
    {
        return _configuration.getBoolean("player-limit", "toggled");
    }

    public static int getPlayerLimit()
    {
        return _configuration.getInt("player-limit", "limit");
    }

    public static int getAttackTime()
    {
        return _configuration.getInt("attack-time");
    }

    public static String getPrefixOption()
    {
        return _configuration.getString("faction-prefix");
    }

    public static boolean shouldDisplayRank()
    {
        return _configuration.getBoolean("faction-rank");
    }

    public static boolean getCreateByItems()
    {
        return _configuration.getBoolean("creating-by-items", "toggled");
    }

    public static HashMap<String, Integer> getRequiredItemsToCreate()
    {
        List<String> itemsList = _configuration.getListOfStrings("creating-by-items", "items");

        HashMap<String, Integer> items = new HashMap<>();

        for (String itemWithAmount : itemsList)
        {
            String strings[] = itemWithAmount.split("\\|");

            String item = strings[0];
            int amount = Integer.valueOf(strings[1]);

            items.put(item, amount);
        }

        return items;
    }

    public static boolean shouldSpawnAtHomeAfterDeath()
    {
        return _configuration.getBoolean("spawn", "spawn-at-home-after-death");
    }

    public static boolean shouldAttackOnlyAtNight()
    {
        return _configuration.getBoolean("attack-only-at-night");
    }

    public static boolean canHomeBetweenWorlds()
    {
        return _configuration.getBoolean("home-from-other-worlds");
    }

    public static int getHomeDelayTime()
    {
        return _configuration.getInt("home-delay");
    }

    public static int getHomeCooldown()
    {
        return _configuration.getInt("home-cooldown");
    }

    public static boolean isDelayedClaimingToggled()
    {
        return _configuration.getBoolean("delayed-claim", "toggled");
    }

    public static int getClaimingDelay()
    {
        return _configuration.getInt("delayed-claim", "claiming-time");
    }

    public static boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        return _configuration.getBoolean("block-home-after-death-in-own-faction", "toggled");
    }

    public static int getHomeBlockTimeAfterDeath()
    {
        return _configuration.getInt("block-home-after-death-in-own-faction", "time");
    }

    public static boolean shouldClaimByItems()
    {
        return _configuration.getBoolean("claiming-by-items", "toggled");
    }

    public static HashMap<String, Integer> getRequiredItemsToClaim()
    {
        List<String> itemsList = _configuration.getListOfStrings("claiming-by-items", "items");

        HashMap<String, Integer> items = new HashMap<>();

        for (String itemWithAmount : itemsList)
        {
            String strings[] = itemWithAmount.split("\\|");

            String item = strings[0];
            int amount = Integer.valueOf(strings[1]);

            items.put(item, amount);
        }

        return items;
    }

    public static double getAttackMinPowerPercentage()
    {
        return _configuration.getDouble("attack-min-power-percentage") / 100;
    }

    public static boolean isPVPLoggerActive()
    {
        return _configuration.getBoolean("pvp-logger", "active");
    }

    public static int getPVPLoggerTime()
    {
        return _configuration.getInt("pvp-logger", "time");
    }

    public static boolean isBlockDestroyingDisabled()
    {
        return _configuration.getBoolean("disable-block-destroy");
    }

    public static boolean isBlockDestroyingInWarZoneDisabled()
    {
        return _configuration.getBoolean("disable-block-destroy-warzone");
    }
}
