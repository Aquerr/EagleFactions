package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.config.ConfigAccess;
import io.github.aquerr.eaglefactions.config.IConfig;
import io.github.aquerr.eaglefactions.config.MainConfig;
import javafx.util.Pair;
import ninja.leaping.configurate.ConfigurationNode;
import sun.java2d.pipe.SpanShapeRenderer;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class MainLogic
{
    private static IConfig mainConfig = MainConfig.getConfig();

    public static boolean isFactionFriendlyFire()
    {
        ConfigurationNode friendlyFireNode = ConfigAccess.getConfig(mainConfig).getNode("friendlyfire-faction");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static boolean isAllianceFriendlyFire()
    {
        ConfigurationNode friendlyFireNode = ConfigAccess.getConfig(mainConfig).getNode("friendlyfire-alliance");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static BigDecimal getGlobalMaxPower()
    {
        ConfigurationNode maxPowerNode = ConfigAccess.getConfig(mainConfig).getNode("power", "maxpower");

        BigDecimal maxPower = new BigDecimal(maxPowerNode.getString());

        return maxPower;
    }

    public static BigDecimal getStartingPower()
    {
        ConfigurationNode startingPowerNode = ConfigAccess.getConfig(mainConfig).getNode("power", "startpower");

        BigDecimal startPower = new BigDecimal(startingPowerNode.getString());

        return startPower;
    }

    public static BigDecimal getPowerIncrement()
    {
        ConfigurationNode powerIncrementNode = ConfigAccess.getConfig(mainConfig).getNode("power", "increment");

        BigDecimal incrementPower = new BigDecimal(powerIncrementNode.getString());

        return incrementPower;
    }

    public static BigDecimal getPowerDecrement()
    {
        ConfigurationNode powerDecrementNode = ConfigAccess.getConfig(mainConfig).getNode("power", "decrement");

        BigDecimal decrementPower = new BigDecimal(powerDecrementNode.getString());

        return decrementPower;
    }

    public static BigDecimal getKillAward()
    {
        ConfigurationNode killAwardNode = ConfigAccess.getConfig(mainConfig).getNode("power", "killaward");

        BigDecimal killAward = new BigDecimal(killAwardNode.getString());

        return killAward;
    }

    public static BigDecimal getPunishment()
    {
        ConfigurationNode punishmentNode = ConfigAccess.getConfig(mainConfig).getNode("power", "punishment");

        BigDecimal punishment = new BigDecimal(punishmentNode.getString());

        return punishment;
    }

    public static int getMaxNameLength()
    {
        ConfigurationNode maxLengthNode = ConfigAccess.getConfig(mainConfig).getNode("name", "max-length");

        int maxLength = maxLengthNode.getInt();

        return maxLength;
    }

    public static int getMinNameLength()
    {
        ConfigurationNode minLengthNode = ConfigAccess.getConfig(mainConfig).getNode("name", "min-length");

        int minLength = minLengthNode.getInt();

        return minLength;
    }

    public static int getMaxTagLength()
    {
        ConfigurationNode maxLengthNode = ConfigAccess.getConfig(mainConfig).getNode("tag", "max-length");

        int maxLength = maxLengthNode.getInt();

        return maxLength;
    }

    public static int getMinTagLength()
    {
        ConfigurationNode minLengthNode = ConfigAccess.getConfig(mainConfig).getNode("tag", "min-length");

        int minLength = minLengthNode.getInt();

        return minLength;
    }

    public static boolean getMobSpawning()
    {
        ConfigurationNode mobSpawningNode = ConfigAccess.getConfig(mainConfig).getNode("spawn", "mobs");

        boolean mobSpawning = mobSpawningNode.getBoolean();

        return mobSpawning;
    }

    public static boolean getBlockEnteringFactions()
    {
        ConfigurationNode enteringFactionsNode = ConfigAccess.getConfig(mainConfig).getNode("block-entering-faction-while-offline");

        boolean enteringFactions = enteringFactionsNode.getBoolean();

        return enteringFactions;
    }

    public static boolean requireConnectedClaims()
    {
        ConfigurationNode requireConnectedClaimsNode = ConfigAccess.getConfig(mainConfig).getNode("connected-claims");

        boolean requireConnectedClaims = requireConnectedClaimsNode.getBoolean();

        return requireConnectedClaims;
    }

    public static boolean shouldBlockSafeZoneFromWarZone()
    {
        ConfigurationNode blockSafeZoneFromWarZoneNode = ConfigAccess.getConfig(mainConfig).getNode("block-safezone-from-warzone");

        boolean blockSafeZoneFromWarZone = blockSafeZoneFromWarZoneNode.getBoolean();

        return blockSafeZoneFromWarZone;
    }

    public static boolean isPlayerLimit()
    {
        ConfigurationNode isPlayerLimitNode = ConfigAccess.getConfig(mainConfig).getNode("player-limit", "toggled");

        boolean playerLimit = isPlayerLimitNode.getBoolean();

        return playerLimit;
    }

    public static int getPlayerLimit()
    {
        ConfigurationNode limitNode = ConfigAccess.getConfig(mainConfig).getNode("player-limit", "limit");

        int limit = limitNode.getInt();

        return limit;
    }

    public static int getAttackTime()
    {
        ConfigurationNode attackTimeNode = ConfigAccess.getConfig(mainConfig).getNode("attack-time");

        int attackTime = attackTimeNode.getInt();

        return attackTime;
    }

    public static String getPrefixOption()
    {
        ConfigurationNode prefixNode = ConfigAccess.getConfig(mainConfig).getNode("faction-prefix");

        String prefix = prefixNode.getString();

        return prefix;
    }

    public static Boolean shouldDisplayRank()
    {
        ConfigurationNode rankNode = ConfigAccess.getConfig(mainConfig).getNode("faction-rank");

        Boolean rank = rankNode.getBoolean();

        return rank;
    }

    public static boolean getCreateByItems()
    {
        ConfigurationNode createByItemsNode = ConfigAccess.getConfig(mainConfig).getNode("creating-by-items", "toggled");

        boolean createByItems = createByItemsNode.getBoolean();

        return createByItems;
    }

    public static HashMap<String, Integer> getRequiredItemsToCreate()
    {
        ConfigurationNode itemsNode = ConfigAccess.getConfig(mainConfig).getNode("creating-by-items", "items");

        List<String> itemsList = itemsNode.getList(objectToStringTransformer);
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
        ConfigurationNode spawnAfterDeathNode = ConfigAccess.getConfig(mainConfig).getNode("spawn", "spawn-at-home-after-death");

        boolean spawnAfterDeath = spawnAfterDeathNode.getBoolean();

        return spawnAfterDeath;
    }

    public static boolean shouldAttackOnlyAtNight()
    {
        ConfigurationNode attackOnlyAtNightNode = ConfigAccess.getConfig(mainConfig).getNode("attack-only-at-night");

        boolean attackAtNight = attackOnlyAtNightNode.getBoolean();

        return attackAtNight;
    }

    public static boolean canHomeBetweenWorlds()
    {
        ConfigurationNode canHomeBetweenWorldsNode = ConfigAccess.getConfig(mainConfig).getNode("home-from-other-worlds");

        boolean canHomeBetweenWorlds = canHomeBetweenWorldsNode.getBoolean();

        return canHomeBetweenWorlds;
    }

    public static boolean isDelayedClaimingToggled()
    {
        ConfigurationNode isDelayedClaimingToggledNode = ConfigAccess.getConfig(mainConfig).getNode("delayed-claim", "toggled");

        boolean isToggled = isDelayedClaimingToggledNode.getBoolean();

        return isToggled;
    }

    public static int getClaimingDelay()
    {
        ConfigurationNode claimingDelayNode = ConfigAccess.getConfig(mainConfig).getNode("delayed-claim", "claiming-time");

        int claimingDelay = claimingDelayNode.getInt();

        return claimingDelay;
    }

    public static boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        ConfigurationNode blockHomeNode = ConfigAccess.getConfig(mainConfig).getNode("block-home-after-death-in-own-faction", "toggled");

        boolean blockHome = blockHomeNode.getBoolean();

        return blockHome;
    }

    public static int getHomeBlockTimeAfterDeath()
    {
        ConfigurationNode blockHomeTimeNode = ConfigAccess.getConfig(mainConfig).getNode("block-home-after-death-in-own-faction", "time");

        int blockHomeTime = blockHomeTimeNode.getInt();

        return blockHomeTime;
    }

    public static boolean shouldClaimByItems()
    {
        ConfigurationNode claimByItemsNode = ConfigAccess.getConfig(mainConfig).getNode("claiming-by-items", "toggled");

        boolean claimByItems = claimByItemsNode.getBoolean();

        return claimByItems;
    }

    public static HashMap<String, Integer> getRequiredItemsToClaim()
    {
        ConfigurationNode itemsNode = ConfigAccess.getConfig(mainConfig).getNode("claiming-by-items", "items");

        List<String> itemsList = itemsNode.getList(objectToStringTransformer);
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

    private static Function<Object,String> objectToStringTransformer = input ->
    {
        if (input instanceof String)
        {
            return (String) input;
        }
        else
        {
            return null;
        }
    };
}
