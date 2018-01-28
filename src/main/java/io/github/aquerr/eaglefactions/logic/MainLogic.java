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

    public static String getPrefixOption()
    {
        ConfigurationNode prefixNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "chat", "factionprefix");

        String prefix = prefixNode.getString();

        return prefix;
    }

    public static Boolean shouldDisplayRank()
    {
        ConfigurationNode rankNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "chat", "factionrank");

        Boolean rank = rankNode.getBoolean();

        return rank;
    }

    public static boolean getCreateByItems()
    {
        ConfigurationNode createByItemsNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "gameplay", "factioncreation", "createbyitems");

        boolean createByItems = createByItemsNode.getBoolean();

        return createByItems;
    }

    public static HashMap<String, Integer> getRequiredItemsToCreate()
    {
        ConfigurationNode itemsNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "gameplay", "factioncreation", "items");

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
        ConfigurationNode spawnAfterDeathNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "spawn", "spawnAtHomeAfterDeath");

        boolean spawnAfterDeath = spawnAfterDeathNode.getBoolean();

        return spawnAfterDeath;
    }

    public static boolean shouldAttackOnlyAtNight()
    {
        ConfigurationNode attackOnlyAtNightNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "gameplay", "attackOnlyAtNight");

        boolean attackAtNight = attackOnlyAtNightNode.getBoolean();

        return attackAtNight;
    }

    public static boolean canHomeBetweenWorlds()
    {
        ConfigurationNode canHomeBetweenWorldsNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "home", "teleportBetweenWorlds");

        boolean canHomeBetweenWorlds = canHomeBetweenWorldsNode.getBoolean();

        return canHomeBetweenWorlds;
    }

    public static boolean isDelayedClaimingToggled()
    {
        ConfigurationNode isDelayedClaimingToggledNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "claims", "Delayed_Claim");

        boolean isToggled = isDelayedClaimingToggledNode.getBoolean();

        return isToggled;
    }

    public static int getClaimingDelay()
    {
        ConfigurationNode claimingDelayNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "claims", "Claiming_Time");

        int claimingDelay = claimingDelayNode.getInt();

        return claimingDelay;
    }

    public static boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        ConfigurationNode blockHomeNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "home", "Block_Home_After_Death_In_Own_Faction", "Turned_On");

        boolean blockHome = blockHomeNode.getBoolean();

        return blockHome;
    }

    public static int getHomeBlockTimeAfterDeath()
    {
        ConfigurationNode blockHomeTimeNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "home", "Block_Home_After_Death_In_Own_Faction", "Time");

        int blockHomeTime = blockHomeTimeNode.getInt();

        return blockHomeTime;
    }

    public static boolean shouldClaimByItems()
    {
        ConfigurationNode claimByItemsNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "claims", "Claiming_By_Items", "Turned_On");

        boolean claimByItems = claimByItemsNode.getBoolean();

        return claimByItems;
    }

    public static HashMap<String, Integer> getRequiredItemsToClaim()
    {
        ConfigurationNode itemsNode = ConfigAccess.getConfig(mainConfig).getNode("eaglefactions", "claims", "Claiming_By_Items", "Items");

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
