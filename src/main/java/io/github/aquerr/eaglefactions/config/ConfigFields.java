package io.github.aquerr.eaglefactions.config;

import com.google.inject.Singleton;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.*;

public final class ConfigFields
{
    private IConfiguration _configuration;

    private String _languageFile = "english.conf";

    private boolean _isFactionFriendlyFire = false;
    private boolean _isAllianceFriendlyFire = false;
    private BigDecimal _globalMaxPower = BigDecimal.valueOf(10.0);
    private BigDecimal _startingPower = BigDecimal.valueOf(5.0);
    private BigDecimal _powerIncrement = BigDecimal.valueOf(0.04);
    private BigDecimal _powerDecrement = BigDecimal.valueOf(2.00);
    private BigDecimal _killAward = BigDecimal.valueOf(2.00);
    private BigDecimal _penalty = BigDecimal.valueOf(1.0);
    private int _maxNameLength = 30;
    private int _minNameLength = 3;
    private int _maxTagLength = 5;
    private int _minTagLength = 2;
    private boolean _mobSpawning = false;
    private boolean _blockEnteringOfflineFactions = false;
    private boolean _requireConnectedClaims = true;
    private boolean _blockEnteringSafezoneFromWarzone = false;
    private boolean _isPlayerLimit = false;
    private int _playerLimit = 15;
    private int _attackTime = 10;
    private String _chatPrefixType = "tag";
    private boolean _shouldDisplayRank = true;
    private boolean _factionCreationByItems = false;
    private HashMap<String,Integer> _requiredItemsToCreateFaction = new HashMap<>();
    private boolean _spawnAtHomeAfterDeath = false;
    private boolean _canAttackOnlyAtNight = false;
    private boolean _canHomeBetweenWorlds = false;
    private int _homeDelay = 5;
    private int _homeCooldown = 60;
    private boolean _shouldDelayClaim;
    private int _claimDelay = 10;
    private boolean _blockHomeAfterDeathInOwnFaction = false;
    private int _homeBlockTimeAfterDeathInOwnFaction = 60;
    private boolean _claimByItems = false;
    private HashMap<String,Integer> _requiredItemsToClaim = new HashMap<>();
    private double _neededPowerPercentageToAttack = 20;
    private boolean _isPvpLoggerActive = true;
    private int _pvpLoggerBlockTime = 60;
    private boolean _disableBlockDestroyAtClaims = false;
    private boolean _disableBlockDestroyAtWarzone = false;
    private List<String> _blockedCommandsDuringFight = Arrays.asList("/f home", "spawn", "tpa", "/tp");
    private boolean _canColorTags = true;
    private Text _factionStartPrefix = Text.of("[");
    private Text _factionEndPrefix = Text.of("]");
    private List<String> _claimableWorldNames = new ArrayList<>();
    private List<String> _notClaimableWorldNames = new ArrayList<>();
    private List<String> _safezoneWorldNames = new ArrayList<>();
    private List<String> _warzoneWorldNames = new ArrayList<>();
    private boolean _isFactionPrefixFirstInChat = true;
    private String _maxInactiveTime = "0";
    private String _storageType = "hocon";
    private String _storageUserName = "sa";
    private String _storagePassword = "";

    public ConfigFields(IConfiguration configuration)
    {
        _configuration = configuration;
        setupConfigFields();
    }

    private void setupConfigFields()
    {
        try
        {
            this._languageFile = _configuration.getString("english.conf", "language-file");

            this._isFactionFriendlyFire = _configuration.getBoolean(false, "friendlyfire-faction");
            this._isAllianceFriendlyFire = _configuration.getBoolean(false, "friendlyfire-alliance");
            this._globalMaxPower = new BigDecimal(_configuration.getString("10.0", "power", "max-power"));
            this._startingPower = new BigDecimal(_configuration.getString("5.0", "power", "start-power"));
            this._powerIncrement = new BigDecimal(_configuration.getString("0.04", "power", "increment"));
            this._powerDecrement = new BigDecimal(_configuration.getString("2.0", "power", "decrement"));
            this._killAward = new BigDecimal(_configuration.getString("2.0", "power", "killaward"));
            this._penalty = new BigDecimal(_configuration.getString("1.0", "power", "penalty"));
            this._maxNameLength = _configuration.getInt(30,"name", "max-length");
            this._minNameLength = _configuration.getInt(3, "name", "min-length");
            this._maxTagLength = _configuration.getInt(5, "tag", "max-length");
            this._minTagLength = _configuration.getInt(2, "tag", "min-length");
            this._mobSpawning = _configuration.getBoolean(false,"spawn", "mobs");
            this._blockEnteringOfflineFactions = _configuration.getBoolean(true, "block-entering-faction-while-offline");
            this._requireConnectedClaims = _configuration.getBoolean(true, "connected-claims");
            this._blockEnteringSafezoneFromWarzone = _configuration.getBoolean(false, "block-safezone-from-warzone");
            this._isPlayerLimit = _configuration.getBoolean(false, "player-limit", "toggled");
            this._playerLimit = _configuration.getInt(15, "player-limit", "limit");
            this._attackTime = _configuration.getInt(10, "attack-time");
            this._chatPrefixType = _configuration.getString("tag", "faction-prefix");
            this._shouldDisplayRank = _configuration.getBoolean(true, "faction-rank");
            this._factionCreationByItems = _configuration.getBoolean(false, "creating-by-items", "toggled");
            this._requiredItemsToCreateFaction = prepareItems(_configuration.getListOfStrings(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20"), "creating-by-items", "items"));
            this._spawnAtHomeAfterDeath = _configuration.getBoolean(false, "spawn", "spawn-at-home-after-death");
            this._canAttackOnlyAtNight = _configuration.getBoolean(false, "attack-only-at-night");
            this._canHomeBetweenWorlds = _configuration.getBoolean(false, "home-from-other-worlds");
            this._homeDelay = _configuration.getInt(5, "home-delay");
            this._homeCooldown = _configuration.getInt(60, "home-cooldown");
            this._shouldDelayClaim = _configuration.getBoolean(false, "delayed-claim", "toggled");
            this._claimDelay = _configuration.getInt(10, "delayed-claim", "claiming-time");
            this._blockHomeAfterDeathInOwnFaction = _configuration.getBoolean(false, "block-home-after-death-in-own-faction", "toggled");
            this._homeBlockTimeAfterDeathInOwnFaction = _configuration.getInt(60, "block-home-after-death-in-own-faction", "time");
            this._claimByItems = _configuration.getBoolean(false, "claiming-by-items", "toggled");
            this._requiredItemsToClaim = prepareItems(_configuration.getListOfStrings(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20", "minecraft:iron_ingot|4"), "claiming-by-items", "items"));
            this._neededPowerPercentageToAttack = _configuration.getDouble(20, "attack-min-power-percentage") / 100;
            this._isPvpLoggerActive = _configuration.getBoolean(true, "pvp-logger", "active");
            this._pvpLoggerBlockTime = _configuration.getInt(60, "pvp-logger", "time");
            this._disableBlockDestroyAtClaims = _configuration.getBoolean(false, "disable-block-destroy-claims");
            this._disableBlockDestroyAtWarzone = _configuration.getBoolean(false, "disable-block-destroy-warzone");
            this._blockedCommandsDuringFight = _configuration.getListOfStrings(Arrays.asList("/f home", "spawn", "tpa", "/tp"), "pvp-logger", "blocked-commands-during-fight");
            this._canColorTags = _configuration.getBoolean(true, "colored-tags-allowed");
            this._factionStartPrefix = TextSerializers.FORMATTING_CODE.deserialize(_configuration.getString("[", "faction-prefix-start"));
            this._factionEndPrefix = TextSerializers.FORMATTING_CODE.deserialize(_configuration.getString("]", "faction-prefix-end"));
            this._claimableWorldNames = _configuration.getListOfStrings(Collections.singletonList(""), "worlds", "CLAIMABLE");
            this._notClaimableWorldNames = _configuration.getListOfStrings(Collections.singletonList(""), "worlds", "NOT_CLAIMABLE");
            this._safezoneWorldNames = _configuration.getListOfStrings(Collections.singletonList(""), "worlds", "SAFE_ZONE");
            this._warzoneWorldNames = _configuration.getListOfStrings(Collections.singletonList(""), "worlds", "WAR_ZONE");
            this._isFactionPrefixFirstInChat = _configuration.getBoolean(true, "faction-prefix-first-in-chat");
            this._maxInactiveTime = _configuration.getString("30d", "max-inactive-time");
            this._storageType = _configuration.getString("hocon", "storage", "type");
            this._storageUserName = _configuration.getString("sa", "storage", "username");
            this._storagePassword = _configuration.getString("", "storage", "password");
            this._configuration.save();
        }
        catch(Exception exception)
        {
            System.out.println("Your faction's config file may be corrupted.");
        }
    }

    public boolean isFactionFriendlyFire()
    {
        return this._isFactionFriendlyFire;
    }

    public boolean isAllianceFriendlyFire()
    {
        return this._isAllianceFriendlyFire;
    }

    public BigDecimal getGlobalMaxPower()
    {
        return this._globalMaxPower;
    }

    public BigDecimal getStartingPower()
    {
        return this._startingPower;
    }

    public BigDecimal getPowerIncrement()
    {
        return this._powerIncrement;
    }

    public BigDecimal getPowerDecrement()
    {
        return this._powerDecrement;
    }

    public BigDecimal getKillAward()
    {
        return this._killAward;
    }

    public BigDecimal getPenalty()
    {
        return this._penalty;
    }

    public int getMaxNameLength()
    {
        return this._maxNameLength;
    }

    public int getMinNameLength()
    {
        return this._minNameLength;
    }

    public int getMaxTagLength()
    {
        return this._maxTagLength;
    }

    public int getMinTagLength()
    {
        return this._minTagLength;
    }

    public boolean getMobSpawning()
    {
        return this._mobSpawning;
    }

    public boolean getBlockEnteringFactions()
    {
        return this._blockEnteringOfflineFactions;
    }

    public boolean requireConnectedClaims()
    {
        return this._requireConnectedClaims;
    }

    public boolean shouldBlockEnteringSafezoneFromWarzone()
    {
        return this._blockEnteringSafezoneFromWarzone;
    }

    public boolean isPlayerLimit()
    {
        return this._isPlayerLimit;
    }

    public int getPlayerLimit()
    {
        return this._playerLimit;
    }

    public int getAttackTime()
    {
        return this._attackTime;
    }

    public String getChatPrefixType()
    {
        return _chatPrefixType;
    }

    public boolean shouldDisplayRank()
    {
        return _shouldDisplayRank;
    }

    public boolean getFactionCreationByItems()
    {
        return _factionCreationByItems;
    }

    private HashMap<String, Integer> prepareItems(List<String> itemsToPrepare)
    {
        HashMap<String, Integer> items = new HashMap<>();

        for (String itemWithAmount : itemsToPrepare)
        {
            String strings[] = itemWithAmount.split("\\|");

            String item = strings[0];
            int amount = Integer.valueOf(strings[1]);

            items.put(item, amount);
        }

        return items;
    }

    public HashMap<String, Integer> getRequiredItemsToCreateFaction()
    {
        return _requiredItemsToCreateFaction;
    }

    public boolean shouldSpawnAtHomeAfterDeath()
    {
        return _spawnAtHomeAfterDeath;
    }

    public boolean canAttackOnlyAtNight()
    {
        return _canAttackOnlyAtNight;
    }

    public boolean canHomeBetweenWorlds()
    {
        return _canHomeBetweenWorlds;
    }

    public int getHomeDelayTime()
    {
        return _homeDelay;
    }

    public int getHomeCooldown()
    {
        return _homeCooldown;
    }

    public boolean shouldDelayClaim()
    {
        return _shouldDelayClaim;
    }

    public int getClaimDelay()
    {
        return _claimDelay;
    }

    public boolean shouldBlockHomeAfterDeathInOwnFaction()
    {
        return _blockHomeAfterDeathInOwnFaction;
    }

    public int getHomeBlockTimeAfterDeathInOwnFaction()
    {
        return _homeBlockTimeAfterDeathInOwnFaction;
    }

    public boolean shouldClaimByItems()
    {
        return _claimByItems;
    }

    public HashMap<String, Integer> getRequiredItemsToClaim()
    {
        return _requiredItemsToClaim;
    }

    public double getNeededPowerPercentageToAttack()
    {
        return _neededPowerPercentageToAttack;
    }

    public boolean isPVPLoggerActive()
    {
        return _isPvpLoggerActive;
    }

    public int getPVPLoggerBlockTime()
    {
        return _pvpLoggerBlockTime;
    }

    public boolean isBlockDestroyAtClaimsDisabled()
    {
        return _disableBlockDestroyAtClaims;
    }

    public boolean isBlockDestroyAtWarzoneDisabled()
    {
        return _disableBlockDestroyAtWarzone;
    }

    public List<String> getBlockedCommandsDuringFight()
    {
        return _blockedCommandsDuringFight;
    }

    public String getLanguageFileName()
    {
        return _languageFile;
    }

    public boolean canColorTags()
    {
        return _canColorTags;
    }

    public Text getFactionStartPrefix()
    {
        return _factionStartPrefix;
    }

    public Text getFactionEndPrefix()
    {
        return _factionEndPrefix;
    }

    public List<String> getClaimableWorldNames()
    {
        return _claimableWorldNames;
    }

    public List<String> getNotClaimableWorldNames()
    {
        return _notClaimableWorldNames;
    }

    public List<String> getSafeZoneWorldNames()
    {
        return _safezoneWorldNames;
    }

    public List<String> getWarZoneWorldNames()
    {
        return _warzoneWorldNames;
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
        return _isFactionPrefixFirstInChat;
    }

    public void addWorld(String worldName)
    {
        _claimableWorldNames.add(worldName);
        _configuration.setListOfStrings(_claimableWorldNames, "worlds", "CLAIMABLE");
    }

    public long getMaxInactiveTime()
    {
        char lastCharacter = _maxInactiveTime.charAt(_maxInactiveTime.length() - 1);

        if(_maxInactiveTime.charAt(0) == '0')
        {
            return 0;
        }
        else if('d' == lastCharacter || 'D' == lastCharacter)
        {
            return Long.parseLong(_maxInactiveTime.substring(0, _maxInactiveTime.length() - 1)) * 24 * 60 * 60;
        }
        else if('h' == lastCharacter || 'H' == lastCharacter)
        {
            return Long.parseLong(_maxInactiveTime.substring(0, _maxInactiveTime.length() - 1)) * 60 * 60;
        }
        else if('m' == lastCharacter || 'M' == lastCharacter)
        {
            return Long.parseLong(_maxInactiveTime.substring(0, _maxInactiveTime.length() - 1)) * 60;
        }
        else if('s' == lastCharacter || 'S' == lastCharacter)
        {
            return Long.parseLong(_maxInactiveTime.substring(0, _maxInactiveTime.length() - 1));
        }

        //Default 10 days
        return 864000;
    }

    public String getStorageType()
    {
        return _storageType;
    }

    public String getStorageUserName()
    {
        return _storageUserName;
    }

    public String getStoragePassword()
    {
        return _storagePassword;
    }
}
