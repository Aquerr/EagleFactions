package io.github.aquerr.eaglefactions.logic;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.config.Configuration;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.*;

@Singleton
public class MainLogic
{
    private static Configuration configuration;

    @Inject
    MainLogic(Configuration configuration){
        MainLogic.configuration = configuration;
    }

    public static boolean isFactionFriendlyFire()
    {
        return configuration.getBoolean(false, "friendlyfire-faction");
    }

    public static boolean isAllianceFriendlyFire()
    {
        return configuration.getBoolean(false, "friendlyfire-alliance");
    }

    public static BigDecimal getGlobalMaxPower()
    {
        return new BigDecimal(configuration.getString("10.0", "power", "max-power"));
    }

    public static BigDecimal getStartingPower()
    {
        return new BigDecimal(configuration.getString("5.0", "power", "start-power"));
    }

    public static BigDecimal getPowerIncrement()
    {
        return new BigDecimal(configuration.getString("0.04", "power", "increment"));
    }

    public static BigDecimal getPowerDecrement()
    {
        return new BigDecimal(configuration.getString("2.0", "power", "decrement"));
    }

    public static BigDecimal getKillAward()
    {
        return new BigDecimal(configuration.getString("2.0", "power", "killaward"));
    }

    public static BigDecimal getPenalty()
    {
        return new BigDecimal(configuration.getString("1.0", "power", "penalty"));
    }

    public static int getMaxNameLength()
    {
        return configuration.getInt(30, "name", "max-length");
    }

    public static int getMinNameLength()
    {
        return configuration.getInt(3, "name", "min-length");
    }

    public static int getMaxTagLength()
    {
        return configuration.getInt(5, "tag", "max-length");
    }

    public static int getMinTagLength()
    {
        return configuration.getInt(2, "tag", "min-length");
    }

    public static boolean getMobSpawning()
    {
        return configuration.getBoolean(false, "spawn", "mobs");
    }

    public static boolean getBlockEnteringFactions()
    {
        return configuration.getBoolean(true, "block-entering-faction-while-offline");
    }

    public static boolean requireConnectedClaims()
    {
        return configuration.getBoolean(true, "connected-claims");
    }

    public static boolean shouldBlockSafeZoneFromWarZone()
    {
        return configuration.getBoolean(false, "block-safezone-from-warzone");
    }

    public static boolean isPlayerLimit()
    {
        return configuration.getBoolean(false, "player-limit", "toggled");
    }

    public static boolean isPeriodicSaving()
    {
        return configuration.getBoolean(true, "saving", "periodic");
    }

    public static int getSaveDelay()
    {
        return configuration.getInt(10, "saving", "delay");
    }

    public static int getPlayerLimit()
    {
        return configuration.getInt(15, "player-limit", "limit");
    }

    public static int getAttackTime()
    {
        return configuration.getInt(10, "attack-time");
    }

    public static String getPrefixOption()
    {
        return configuration.getString("tag", "faction-prefix");
    }

    public static boolean shouldDisplayRank()
    {
        return configuration.getBoolean(true, "faction-rank");
    }

    public static boolean getCreateByItems()
    {
        return configuration.getBoolean(false, "creating-by-items", "toggled");
    }

    public static HashMap<String, Integer> getRequiredItemsToCreate()
    {
        List<String> itemsList = configuration.getListOfStrings(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20"), "creating-by-items", "items");

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
        return configuration.getBoolean(false, "spawn", "spawn-at-home-after-death");
    }

    public static boolean shouldAttackOnlyAtNight()
    {
        return configuration.getBoolean(false, "attack-only-at-night");
    }

    public static boolean canHomeBetweenWorlds()
    {
        return configuration.getBoolean(false, "home-from-other-worlds");
    }

    public static int getHomeDelayTime()
    {
        return configuration.getInt(5, "home-delay");
    }

    public static int getHomeCooldown()
    {
        return configuration.getInt(60, "home-cooldown");
    }

    public static boolean isDelayedClaimingToggled()
    {
        return configuration.getBoolean(false, "delayed-claim", "toggled");
    }

    public static int getClaimingDelay()
    {
        return configuration.getInt(10, "delayed-claim", "claiming-time");
    }

    public static boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        return configuration.getBoolean(false, "block-home-after-death-in-own-faction", "toggled");
    }

    public static int getHomeBlockTimeAfterDeath()
    {
        return configuration.getInt(60, "block-home-after-death-in-own-faction", "time");
    }

    public static boolean shouldClaimByItems()
    {
        return configuration.getBoolean(false, "claiming-by-items", "toggled");
    }

    public static HashMap<String, Integer> getRequiredItemsToClaim()
    {
        List<String> itemsList = configuration.getListOfStrings(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20", "minecraft:iron_ingot|4"), "claiming-by-items", "items");

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
        return configuration.getDouble(20, "attack-min-power-percentage") / 100;
    }

    public static boolean isPVPLoggerActive()
    {
        return configuration.getBoolean(true, "pvp-logger", "active");
    }

    public static int getPVPLoggerTime()
    {
        return configuration.getInt(60, "pvp-logger", "time");
    }

    public static boolean isBlockDestroyingDisabled()
    {
        return configuration.getBoolean(false, "disable-block-destroy-claims");
    }

    public static boolean isBlockDestroyingInWarZoneDisabled()
    {
        return configuration.getBoolean(false, "disable-block-destroy-warzone");
    }

    public static List<String> getBlockedCommandsDuringFight()
    {
        return configuration.getListOfStrings(Arrays.asList("/f home", "spawn", "tpa", "/tp"), "pvp-logger", "blocked-commands-during-fight");
    }

    public static String getLanguageFileName()
    {
        return configuration.getString("english.conf", "language-file");
    }

    public static boolean areColoredTagsAllowed()
    {
        return configuration.getBoolean(true, "colored-tags-allowed");
    }

    public static Text getFactionPrefixStart()
    {
        String prefix = configuration.getString("[", "faction-prefix-start");
        return TextSerializers.FORMATTING_CODE.deserialize(prefix);
    }

    public static Text getFactionPrefixEnd()
    {
        String prefix = configuration.getString("]", "faction-prefix-end");
        return TextSerializers.FORMATTING_CODE.deserialize(prefix);
    }

    public static List<String> getClaimableWorldNames()
    {
        return configuration.getListOfStrings(Collections.singletonList(""), "worlds", "CLAIMABLE");
    }

    public static List<String> getNotClaimableWorldNames()
    {
        return configuration.getListOfStrings(Collections.singletonList(""), "worlds", "NOT_CLAIMABLE");
    }

    public static List<String> getSafeZoneWorldNames()
    {
        return configuration.getListOfStrings(Collections.singletonList(""), "worlds", "SAFE_ZONE");
    }

    public static List<String> getWarZoneWorldNames()
    {
        return configuration.getListOfStrings(Collections.singletonList(""), "worlds", "WAR_ZONE");
    }

    public static List<String> getDetectedWorldNames()
    {
        List<String> detectedWorldNames = new ArrayList<>();

        detectedWorldNames.addAll(getClaimableWorldNames());
        detectedWorldNames.addAll(getNotClaimableWorldNames());
        detectedWorldNames.addAll(getSafeZoneWorldNames());
        detectedWorldNames.addAll(getWarZoneWorldNames());

        return detectedWorldNames;
    }

    public static boolean isFactionPrefixFirstInChat()
    {
        return configuration.getBoolean(true, "faction-prefix-first-in-chat");
    }

    public static void addWorld(String worldName)
    {
        List<String> claimableWorldNames = new ArrayList<>(getClaimableWorldNames());
        claimableWorldNames.add(worldName);

        configuration.setListOfStrings(claimableWorldNames, "worlds", "CLAIMABLE");
    }
}
