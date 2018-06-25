package io.github.aquerr.eaglefactions.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.*;

@Singleton
public class Settings
{
    private Configuration configuration;

    @Inject
    Settings(Configuration configuration){
        this.configuration = configuration;
    }

    public boolean isFactionFriendlyFire()
    {
        return configuration.getBoolean(false, "friendlyfire-faction");
    }

    public boolean isAllianceFriendlyFire()
    {
        return configuration.getBoolean(false, "friendlyfire-alliance");
    }

    public BigDecimal getGlobalMaxPower()
    {
        return new BigDecimal(configuration.getString("10.0", "power", "max-power"));
    }

    public BigDecimal getStartingPower()
    {
        return new BigDecimal(configuration.getString("5.0", "power", "start-power"));
    }

    public BigDecimal getPowerIncrement()
    {
        return new BigDecimal(configuration.getString("0.04", "power", "increment"));
    }

    public BigDecimal getPowerDecrement()
    {
        return new BigDecimal(configuration.getString("2.0", "power", "decrement"));
    }

    public BigDecimal getKillAward()
    {
        return new BigDecimal(configuration.getString("2.0", "power", "killaward"));
    }

    public BigDecimal getPenalty()
    {
        return new BigDecimal(configuration.getString("1.0", "power", "penalty"));
    }

    public int getMaxNameLength()
    {
        return configuration.getInt(30, "name", "max-length");
    }

    public int getMinNameLength()
    {
        return configuration.getInt(3, "name", "min-length");
    }

    public int getMaxTagLength()
    {
        return configuration.getInt(5, "tag", "max-length");
    }

    public int getMinTagLength()
    {
        return configuration.getInt(2, "tag", "min-length");
    }

    public boolean getMobSpawning()
    {
        return configuration.getBoolean(false, "spawn", "mobs");
    }

    public boolean getBlockEnteringFactions()
    {
        return configuration.getBoolean(true, "block-entering-faction-while-offline");
    }

    public boolean requireConnectedClaims()
    {
        return configuration.getBoolean(true, "connected-claims");
    }

    public boolean shouldBlockSafeZoneFromWarZone()
    {
        return configuration.getBoolean(false, "block-safezone-from-warzone");
    }

    public boolean isPlayerLimit()
    {
        return configuration.getBoolean(false, "player-limit", "toggled");
    }

    public boolean isPeriodicSaving()
    {
        if(configuration == null){
            System.out.println("AHHHHH something went wrong!");
        }
        return configuration.getBoolean(true, "saving", "periodic");
    }

    public int getSaveDelay()
    {
        return configuration.getInt(10, "saving", "delay");
    }

    public int getPlayerLimit()
    {
        return configuration.getInt(15, "player-limit", "limit");
    }

    public int getAttackTime()
    {
        return configuration.getInt(10, "attack-time");
    }

    public String getPrefixOption()
    {
        return configuration.getString("tag", "faction-prefix");
    }

    public boolean shouldDisplayRank()
    {
        return configuration.getBoolean(true, "faction-rank");
    }

    public boolean getCreateByItems()
    {
        return configuration.getBoolean(false, "creating-by-items", "toggled");
    }

    public HashMap<String, Integer> getRequiredItemsToCreate()
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

    public boolean shouldSpawnAtHomeAfterDeath()
    {
        return configuration.getBoolean(false, "spawn", "spawn-at-home-after-death");
    }

    public boolean shouldAttackOnlyAtNight()
    {
        return configuration.getBoolean(false, "attack-only-at-night");
    }

    public boolean canHomeBetweenWorlds()
    {
        return configuration.getBoolean(false, "home-from-other-worlds");
    }

    public int getHomeDelayTime()
    {
        return configuration.getInt(5, "home-delay");
    }

    public int getHomeCooldown()
    {
        return configuration.getInt(60, "home-cooldown");
    }

    public boolean isDelayedClaimingToggled()
    {
        return configuration.getBoolean(false, "delayed-claim", "toggled");
    }

    public int getClaimingDelay()
    {
        return configuration.getInt(10, "delayed-claim", "claiming-time");
    }

    public boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        return configuration.getBoolean(false, "block-home-after-death-in-own-faction", "toggled");
    }

    public int getHomeBlockTimeAfterDeath()
    {
        return configuration.getInt(60, "block-home-after-death-in-own-faction", "time");
    }

    public boolean shouldClaimByItems()
    {
        return configuration.getBoolean(false, "claiming-by-items", "toggled");
    }

    public HashMap<String, Integer> getRequiredItemsToClaim()
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

    public double getAttackMinPowerPercentage()
    {
        return configuration.getDouble(20, "attack-min-power-percentage") / 100;
    }

    public boolean isPVPLoggerActive()
    {
        return configuration.getBoolean(true, "pvp-logger", "active");
    }

    public int getPVPLoggerTime()
    {
        return configuration.getInt(60, "pvp-logger", "time");
    }

    public boolean isBlockDestroyingDisabled()
    {
        return configuration.getBoolean(false, "disable-block-destroy-claims");
    }

    public boolean isBlockDestroyingInWarZoneDisabled()
    {
        return configuration.getBoolean(false, "disable-block-destroy-warzone");
    }

    public List<String> getBlockedCommandsDuringFight()
    {
        return configuration.getListOfStrings(Arrays.asList("/f home", "spawn", "tpa", "/tp"), "pvp-logger", "blocked-commands-during-fight");
    }

    public String getLanguageFileName()
    {
        return configuration.getString("english.conf", "language-file");
    }

    public boolean areColoredTagsAllowed()
    {
        return configuration.getBoolean(true, "colored-tags-allowed");
    }

    public Text getFactionPrefixStart()
    {
        String prefix = configuration.getString("[", "faction-prefix-start");
        return TextSerializers.FORMATTING_CODE.deserialize(prefix);
    }

    public Text getFactionPrefixEnd()
    {
        String prefix = configuration.getString("]", "faction-prefix-end");
        return TextSerializers.FORMATTING_CODE.deserialize(prefix);
    }

    public List<String> getClaimableWorldNames()
    {
        return configuration.getListOfStrings(Collections.singletonList(""), "worlds", "CLAIMABLE");
    }

    public List<String> getNotClaimableWorldNames()
    {
        return configuration.getListOfStrings(Collections.singletonList(""), "worlds", "NOT_CLAIMABLE");
    }

    public List<String> getSafeZoneWorldNames()
    {
        return configuration.getListOfStrings(Collections.singletonList(""), "worlds", "SAFE_ZONE");
    }

    public List<String> getWarZoneWorldNames()
    {
        return configuration.getListOfStrings(Collections.singletonList(""), "worlds", "WAR_ZONE");
    }

    public List<String> getDetectedWorldNames()
    {
        List<String> detectedWorldNames = new ArrayList<>();

        detectedWorldNames.addAll(getClaimableWorldNames());
        detectedWorldNames.addAll(getNotClaimableWorldNames());
        detectedWorldNames.addAll(getSafeZoneWorldNames());
        detectedWorldNames.addAll(getWarZoneWorldNames());

        return detectedWorldNames;
    }

    public boolean isFactionPrefixFirstInChat()
    {
        return configuration.getBoolean(true, "faction-prefix-first-in-chat");
    }

    public void addWorld(String worldName)
    {
        List<String> claimableWorldNames = new ArrayList<>(getClaimableWorldNames());
        claimableWorldNames.add(worldName);

        configuration.setListOfStrings(claimableWorldNames, "worlds", "CLAIMABLE");
    }
}
