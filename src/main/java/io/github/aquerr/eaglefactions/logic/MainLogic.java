package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.config.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
        return _configuration.getBoolean(false, "friendlyfire-faction");
    }

    public static boolean isAllianceFriendlyFire()
    {
        return _configuration.getBoolean(false, "friendlyfire-alliance");
    }

    public static BigDecimal getGlobalMaxPower()
    {
        return new BigDecimal(_configuration.getString("10.0", "power", "max-power"));
    }

    public static BigDecimal getStartingPower()
    {
        return new BigDecimal(_configuration.getString("5.0", "power", "start-power"));
    }

    public static BigDecimal getPowerIncrement()
    {
        return new BigDecimal(_configuration.getString("0.04", "power", "increment"));
    }

    public static BigDecimal getPowerDecrement()
    {
        return new BigDecimal(_configuration.getString("2.0", "power", "decrement"));
    }

    public static BigDecimal getKillAward()
    {
        return new BigDecimal(_configuration.getString("2.0", "power", "killaward"));
    }

    public static BigDecimal getPenalty()
    {
        return new BigDecimal(_configuration.getString("1.0", "power", "penalty"));
    }

    public static int getMaxNameLength()
    {
        return _configuration.getInt(30,"name", "max-length");
    }

    public static int getMinNameLength()
    {
        return _configuration.getInt(3, "name", "min-length");
    }

    public static int getMaxTagLength()
    {
        return _configuration.getInt(5, "tag", "max-length");
    }

    public static int getMinTagLength()
    {
        return _configuration.getInt(2, "tag", "min-length");
    }

    public static boolean getMobSpawning()
    {
        return _configuration.getBoolean(false,"spawn", "mobs");
    }

    public static boolean getBlockEnteringFactions()
    {
        return _configuration.getBoolean(true, "block-entering-faction-while-offline");
    }

    public static boolean requireConnectedClaims()
    {
        return _configuration.getBoolean(true, "connected-claims");
    }

    public static boolean shouldBlockSafeZoneFromWarZone()
    {
        return _configuration.getBoolean(false, "block-safezone-from-warzone");
    }

    public static boolean isPlayerLimit()
    {
        return _configuration.getBoolean(false, "player-limit", "toggled");
    }

    public static int getPlayerLimit()
    {
        return _configuration.getInt(15, "player-limit", "limit");
    }

    public static int getAttackTime()
    {
        return _configuration.getInt(10, "attack-time");
    }

    public static String getPrefixOption()
    {
        return _configuration.getString("tag", "faction-prefix");
    }

    public static boolean shouldDisplayRank()
    {
        return _configuration.getBoolean(true, "faction-rank");
    }

    public static boolean getCreateByItems()
    {
        return _configuration.getBoolean(false, "creating-by-items", "toggled");
    }

    public static HashMap<String, Integer> getRequiredItemsToCreate()
    {
        List<String> itemsList = _configuration.getListOfStrings(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20"), "creating-by-items", "items");

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
        return _configuration.getBoolean(false, "spawn", "spawn-at-home-after-death");
    }

    public static boolean shouldAttackOnlyAtNight()
    {
        return _configuration.getBoolean(false, "attack-only-at-night");
    }

    public static boolean canHomeBetweenWorlds()
    {
        return _configuration.getBoolean(false, "home-from-other-worlds");
    }

    public static int getHomeDelayTime()
    {
        return _configuration.getInt(5, "home-delay");
    }

    public static int getHomeCooldown()
    {
        return _configuration.getInt(60, "home-cooldown");
    }

    public static boolean isDelayedClaimingToggled()
    {
        return _configuration.getBoolean(false, "delayed-claim", "toggled");
    }

    public static int getClaimingDelay()
    {
        return _configuration.getInt(10, "delayed-claim", "claiming-time");
    }

    public static boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        return _configuration.getBoolean(false, "block-home-after-death-in-own-faction", "toggled");
    }

    public static int getHomeBlockTimeAfterDeath()
    {
        return _configuration.getInt(60, "block-home-after-death-in-own-faction", "time");
    }

    public static boolean shouldClaimByItems()
    {
        return _configuration.getBoolean(false, "claiming-by-items", "toggled");
    }

    public static HashMap<String, Integer> getRequiredItemsToClaim()
    {
        List<String> itemsList = _configuration.getListOfStrings(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20", "minecraft:iron_ingot|4"), "claiming-by-items", "items");

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
        return _configuration.getDouble(20, "attack-min-power-percentage") / 100;
    }

    public static boolean isPVPLoggerActive()
    {
        return _configuration.getBoolean(true, "pvp-logger", "active");
    }

    public static int getPVPLoggerTime()
    {
        return _configuration.getInt(60, "pvp-logger", "time");
    }

    public static boolean isBlockDestroyingDisabled()
    {
        return _configuration.getBoolean(false, "disable-block-destroy-claims");
    }

    public static boolean isBlockDestroyingInWarZoneDisabled()
    {
        return _configuration.getBoolean(false, "disable-block-destroy-warzone");
    }

    public static List<String> getBlockedCommandsDuringFight()
    {
        return _configuration.getListOfStrings(Arrays.asList("/f home", "spawn", "tpa", "/tp"), "pvp-logger", "blocked-commands-during-fight");
    }

    public static String getLanguageFileName()
    {
        return _configuration.getString("english.conf", "language-file");
    }

    public static boolean areColoredTagsAllowed()
    {
        return _configuration.getBoolean(true, "colored-tags-allowed");
    }
}
