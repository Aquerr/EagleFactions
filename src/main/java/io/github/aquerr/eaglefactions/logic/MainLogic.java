package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.config.Configuration;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class MainLogic
{
    private static CommentedConfigurationNode _commentedConfigurationNode;

    public static void setup(Configuration configuration)
    {
        try
        {
            _commentedConfigurationNode = HoconConfigurationLoader.builder().setPath(configuration.getConfigPath()).build().load();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static boolean isFactionFriendlyFire()
    {
        ConfigurationNode friendlyFireNode = _commentedConfigurationNode.getNode("friendlyfire-faction");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static boolean isAllianceFriendlyFire()
    {
        ConfigurationNode friendlyFireNode = _commentedConfigurationNode.getNode("friendlyfire-alliance");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static BigDecimal getGlobalMaxPower()
    {
        ConfigurationNode maxPowerNode = _commentedConfigurationNode.getNode("power", "max-power");

        BigDecimal maxPower = new BigDecimal(maxPowerNode.getString());

        return maxPower;
    }

    public static BigDecimal getStartingPower()
    {
        ConfigurationNode startingPowerNode = _commentedConfigurationNode.getNode("power", "start-power");

        BigDecimal startPower = new BigDecimal(startingPowerNode.getString());

        return startPower;
    }

    public static BigDecimal getPowerIncrement()
    {
        ConfigurationNode powerIncrementNode = _commentedConfigurationNode.getNode("power", "increment");

        BigDecimal incrementPower = new BigDecimal(powerIncrementNode.getString());

        return incrementPower;
    }

    public static BigDecimal getPowerDecrement()
    {
        ConfigurationNode powerDecrementNode = _commentedConfigurationNode.getNode("power", "decrement");

        BigDecimal decrementPower = new BigDecimal(powerDecrementNode.getString());

        return decrementPower;
    }

    public static BigDecimal getKillAward()
    {
        ConfigurationNode killAwardNode = _commentedConfigurationNode.getNode("power", "killaward");

        BigDecimal killAward = new BigDecimal(killAwardNode.getString());

        return killAward;
    }

    public static BigDecimal getPunishment()
    {
        ConfigurationNode punishmentNode = _commentedConfigurationNode.getNode("power", "punishment");

        BigDecimal punishment = new BigDecimal(punishmentNode.getString());

        return punishment;
    }

    public static int getMaxNameLength()
    {
        ConfigurationNode maxLengthNode = _commentedConfigurationNode.getNode("name", "max-length");

        int maxLength = maxLengthNode.getInt();

        return maxLength;
    }

    public static int getMinNameLength()
    {
        ConfigurationNode minLengthNode = _commentedConfigurationNode.getNode("name", "min-length");

        int minLength = minLengthNode.getInt();

        return minLength;
    }

    public static int getMaxTagLength()
    {
        ConfigurationNode maxLengthNode = _commentedConfigurationNode.getNode("tag", "max-length");

        int maxLength = maxLengthNode.getInt();

        return maxLength;
    }

    public static int getMinTagLength()
    {
        ConfigurationNode minLengthNode = _commentedConfigurationNode.getNode("tag", "min-length");

        int minLength = minLengthNode.getInt();

        return minLength;
    }

    public static boolean getMobSpawning()
    {
        ConfigurationNode mobSpawningNode = _commentedConfigurationNode.getNode("spawn", "mobs");

        boolean mobSpawning = mobSpawningNode.getBoolean();

        return mobSpawning;
    }

    public static boolean getBlockEnteringFactions()
    {
        ConfigurationNode enteringFactionsNode = _commentedConfigurationNode.getNode("block-entering-faction-while-offline");

        boolean enteringFactions = enteringFactionsNode.getBoolean();

        return enteringFactions;
    }

    public static boolean requireConnectedClaims()
    {
        ConfigurationNode requireConnectedClaimsNode = _commentedConfigurationNode.getNode("connected-claims");

        boolean requireConnectedClaims = requireConnectedClaimsNode.getBoolean();

        return requireConnectedClaims;
    }

    public static boolean shouldBlockSafeZoneFromWarZone()
    {
        ConfigurationNode blockSafeZoneFromWarZoneNode = _commentedConfigurationNode.getNode("block-safezone-from-warzone");

        boolean blockSafeZoneFromWarZone = blockSafeZoneFromWarZoneNode.getBoolean();

        return blockSafeZoneFromWarZone;
    }

    public static boolean isPlayerLimit()
    {
        ConfigurationNode isPlayerLimitNode = _commentedConfigurationNode.getNode("player-limit", "toggled");

        boolean playerLimit = isPlayerLimitNode.getBoolean();

        return playerLimit;
    }

    public static int getPlayerLimit()
    {
        ConfigurationNode limitNode = _commentedConfigurationNode.getNode("player-limit", "limit");

        int limit = limitNode.getInt();

        return limit;
    }

    public static int getAttackTime()
    {
        ConfigurationNode attackTimeNode = _commentedConfigurationNode.getNode("attack-time");

        int attackTime = attackTimeNode.getInt();

        return attackTime;
    }

    public static String getPrefixOption()
    {
        ConfigurationNode prefixNode = _commentedConfigurationNode.getNode("faction-prefix");

        String prefix = prefixNode.getString();

        return prefix;
    }

    public static Boolean shouldDisplayRank()
    {
        ConfigurationNode rankNode = _commentedConfigurationNode.getNode("faction-rank");

        Boolean rank = rankNode.getBoolean();

        return rank;
    }

    public static boolean getCreateByItems()
    {
        ConfigurationNode createByItemsNode = _commentedConfigurationNode.getNode("creating-by-items", "toggled");

        boolean createByItems = createByItemsNode.getBoolean();

        return createByItems;
    }

    public static HashMap<String, Integer> getRequiredItemsToCreate()
    {
        ConfigurationNode itemsNode = _commentedConfigurationNode.getNode("creating-by-items", "items");

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
        ConfigurationNode spawnAfterDeathNode = _commentedConfigurationNode.getNode("spawn", "spawn-at-home-after-death");

        boolean spawnAfterDeath = spawnAfterDeathNode.getBoolean();

        return spawnAfterDeath;
    }

    public static boolean shouldAttackOnlyAtNight()
    {
        ConfigurationNode attackOnlyAtNightNode = _commentedConfigurationNode.getNode("attack-only-at-night");

        boolean attackAtNight = attackOnlyAtNightNode.getBoolean();

        return attackAtNight;
    }

    public static boolean canHomeBetweenWorlds()
    {
        ConfigurationNode canHomeBetweenWorldsNode = _commentedConfigurationNode.getNode("home-from-other-worlds");

        boolean canHomeBetweenWorlds = canHomeBetweenWorldsNode.getBoolean();

        return canHomeBetweenWorlds;
    }

    public static int getHomeDelayTime()
    {
        ConfigurationNode homeDelayTimeNode = _commentedConfigurationNode.getNode("home-delay");

        int homeDelay = homeDelayTimeNode.getInt();

        return homeDelay;
    }

    public static boolean isDelayedClaimingToggled()
    {
        ConfigurationNode isDelayedClaimingToggledNode = _commentedConfigurationNode.getNode("delayed-claim", "toggled");

        boolean isToggled = isDelayedClaimingToggledNode.getBoolean();

        return isToggled;
    }

    public static int getClaimingDelay()
    {
        ConfigurationNode claimingDelayNode = _commentedConfigurationNode.getNode("delayed-claim", "claiming-time");

        int claimingDelay = claimingDelayNode.getInt();

        return claimingDelay;
    }

    public static boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        ConfigurationNode blockHomeNode = _commentedConfigurationNode.getNode("block-home-after-death-in-own-faction", "toggled");

        boolean blockHome = blockHomeNode.getBoolean();

        return blockHome;
    }

    public static int getHomeBlockTimeAfterDeath()
    {
        ConfigurationNode blockHomeTimeNode = _commentedConfigurationNode.getNode("block-home-after-death-in-own-faction", "time");

        int blockHomeTime = blockHomeTimeNode.getInt();

        return blockHomeTime;
    }

    public static boolean shouldClaimByItems()
    {
        ConfigurationNode claimByItemsNode = _commentedConfigurationNode.getNode("claiming-by-items", "toggled");

        boolean claimByItems = claimByItemsNode.getBoolean();

        return claimByItems;
    }

    public static HashMap<String, Integer> getRequiredItemsToClaim()
    {
        ConfigurationNode itemsNode = _commentedConfigurationNode.getNode("claiming-by-items", "items");

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
