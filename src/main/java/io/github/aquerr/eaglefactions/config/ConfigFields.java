package io.github.aquerr.eaglefactions.config;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;

public final class ConfigFields
{
    private IConfiguration _configuration;

    private String _languageFile = "english.conf";

    private boolean _isFactionFriendlyFire = false;
    private boolean _isAllianceFriendlyFire = false;
    private float _globalMaxPower = 10.0f;
    private float _startingPower = 5.0f;
    private float _powerIncrement = 0.04f;
    private float _powerDecrement = 2.00f;
    private float _killAward = 2.00f;
    private float _penalty = 1.0f;
    private int _maxNameLength = 30;
    private int _minNameLength = 3;
    private int _maxTagLength = 5;
    private int _minTagLength = 2;

    private boolean _spawnMobsInSafeZone = true;
    private boolean _spawnMobsInWarZone = true;
    private boolean _spawnHostileMobsInWarZone = true;
    private boolean _spawnMobsInFactionsTerritory = true;
    private boolean _spawnHostileMobsInFactionsTerritory = true;
//    private boolean _mobSpawning = false;

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
    private boolean _showPvpLoggerInScoreboard = true;
    private boolean _protectFromMobGrief = false;
    private boolean _protectFromMobGriefWarZone = false;
    private boolean _allowExplosionsByOtherPlayersInClaims = false;
    private boolean _protectWarZoneFromPlayers = false;
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
    private boolean _notifyWhenFactionRemoved;
    private boolean _canUseFactionChest = true;
    private boolean _showOnlyPlayersFactionsClaimsInMap = false;

    //Storage
    private String _storageType = "hocon";
    private String _storageUserName = "sa";
    private String _storagePassword = "";
    private String _databaseUrl = "localhost:3306/";
    private String _databaseFileName = "database";

    //Whitelisted items and blocks
    private List<String> _whitelistedItems = new ArrayList<>();
    private List<String> _whitelistedPlaceDestroyBlocks = new ArrayList<>();
    private List<String> _whitelistedInteractBlocks = new ArrayList<>();

    //Chat
    private boolean _supressOtherFactionsMessagesWhileInTeamChat = false;

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
            this._globalMaxPower = _configuration.getFloat(10.0f, "power", "max-power");
            this._startingPower = _configuration.getFloat(5.0f, "power", "start-power");
            this._powerIncrement = _configuration.getFloat(0.04f, "power", "increment");
            this._powerDecrement = _configuration.getFloat(2.0f, "power", "decrement");
            this._killAward = _configuration.getFloat(2.0f, "power", "kill-award");
            this._penalty = _configuration.getFloat(1.0f, "power", "penalty");
            this._maxNameLength = _configuration.getInt(30,"name", "max-length");
            this._minNameLength = _configuration.getInt(3, "name", "min-length");
            this._maxTagLength = _configuration.getInt(5, "tag", "max-length");
            this._minTagLength = _configuration.getInt(2, "tag", "min-length");

            //Mob spawning nodes
            this._spawnMobsInSafeZone = _configuration.getBoolean(true, "spawn-mobs-in-safezone");
            this._spawnMobsInWarZone = _configuration.getBoolean(true, "spawn-mobs-in-warzone");
            this._spawnHostileMobsInWarZone = _configuration.getBoolean(true, "spawn-hostile-mobs-in-warzone");
            this._spawnMobsInFactionsTerritory = _configuration.getBoolean(true, "spawn-mobs-in-factions-territory");
            this._spawnHostileMobsInFactionsTerritory = _configuration.getBoolean(true, "spawn-hostile-mobs-in-factions-territory");

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
            this._spawnAtHomeAfterDeath = _configuration.getBoolean(false, "spawn-at-home-after-death");
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
            this._showPvpLoggerInScoreboard = _configuration.getBoolean(true, "pvp-logger", "show-in-scoreboard");
            this._protectFromMobGrief = _configuration.getBoolean(false, "protect-from-mob-grief");
            this._protectFromMobGriefWarZone = _configuration.getBoolean(false, "protect-from-mob-grief-warzone");
            this._allowExplosionsByOtherPlayersInClaims = _configuration.getBoolean(false, "allow-explosions-by-other-players-in-claims");
            this._protectWarZoneFromPlayers = _configuration.getBoolean(false, "protect-warzone-from-players");
            this._blockedCommandsDuringFight = _configuration.getListOfStrings(Arrays.asList("/f home", "spawn", "tpa", "/tp"), "pvp-logger", "blocked-commands-during-fight");
            this._canColorTags = _configuration.getBoolean(true, "colored-tags-allowed");
            this._factionStartPrefix = TextSerializers.FORMATTING_CODE.deserialize(_configuration.getString("[", "faction-prefix-start"));
            this._factionEndPrefix = TextSerializers.FORMATTING_CODE.deserialize(_configuration.getString("]", "faction-prefix-end"));
            this._claimableWorldNames = new ArrayList<>(_configuration.getListOfStrings(new ArrayList<>(), "worlds", "CLAIMABLE"));
            this._notClaimableWorldNames = new ArrayList<>(_configuration.getListOfStrings(new ArrayList<>(), "worlds", "NOT_CLAIMABLE"));
            this._safezoneWorldNames = new ArrayList<>(_configuration.getListOfStrings(new ArrayList<>(), "worlds", "SAFE_ZONE"));
            this._warzoneWorldNames = new ArrayList<>(_configuration.getListOfStrings(new ArrayList<>(), "worlds", "WAR_ZONE"));
            this._isFactionPrefixFirstInChat = _configuration.getBoolean(true, "faction-prefix-first-in-chat");
            this._maxInactiveTime = _configuration.getString("30d", "factions-remover", "max-inactive-time");
            this._notifyWhenFactionRemoved = _configuration.getBoolean(true, "factions-remover", "notify-when-removed");
            this._canUseFactionChest = _configuration.getBoolean(true, "faction-chest");
            this._showOnlyPlayersFactionsClaimsInMap = _configuration.getBoolean(false, "show-only-player-faction-claims-in-map");

            //Storage
            this._storageType = _configuration.getString("hocon", "storage", "type");
            this._storageUserName = _configuration.getString("sa", "storage", "username");
            this._storagePassword = _configuration.getString("", "storage", "password");
            this._databaseUrl = _configuration.getString("localhost:3306/", "storage", "database-url");
            this._databaseFileName = _configuration.getString("database", "storage", "database-file-name");

            //Whitelisted items and blocks
            this._whitelistedItems = _configuration.getListOfStrings(new ArrayList<>(), "allowed-items-and-blocks", "items-whitelist");
            this._whitelistedPlaceDestroyBlocks = _configuration.getListOfStrings(new ArrayList<>(), "allowed-items-and-blocks", "place-destroy-whitelist");
            this._whitelistedInteractBlocks = _configuration.getListOfStrings(new ArrayList<>(), "allowed-items-and-blocks", "interact-whitelist");

            //Chat
            this._supressOtherFactionsMessagesWhileInTeamChat = _configuration.getBoolean(false, "suppress-other-factions-messages-while-in-team-chat");

            this._configuration.save();
        }
        catch(Exception exception)
        {
            System.out.println("Your faction's config file may be corrupted.");
        }
    }

    public void reload()
    {
        setupConfigFields();
    }

    public boolean isFactionFriendlyFire()
    {
        return this._isFactionFriendlyFire;
    }

    public boolean isAllianceFriendlyFire()
    {
        return this._isAllianceFriendlyFire;
    }

    public float getGlobalMaxPower()
    {
        return this._globalMaxPower;
    }

    public float getStartingPower()
    {
        return this._startingPower;
    }

    public float getPowerIncrement()
    {
        return this._powerIncrement;
    }

    public float getPowerDecrement()
    {
        return this._powerDecrement;
    }

    public float getKillAward()
    {
        return this._killAward;
    }

    public float getPenalty()
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

    //Mob spawning methods
    public boolean canSpawnMobsInSafeZone()
    {
        return this._spawnMobsInSafeZone;
    }

    public boolean canSpawnMobsInWarZone()
    {
        return this._spawnMobsInWarZone;
    }

    public boolean canSpawnHostileMobsInWarZone()
    {
        return this._spawnHostileMobsInWarZone;
    }

    public boolean canSpawnMobsInFactionsTerritory()
    {
        return this._spawnMobsInFactionsTerritory;
    }

    public boolean canSpawnHostileMobsInFactionsTerritory()
    {
        return this._spawnHostileMobsInFactionsTerritory;
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

    public boolean shouldProtectClaimFromMobGrief()
    {
        return _protectFromMobGrief;
    }

    public boolean shouldProtectWarZoneFromMobGrief()
    {
        return _protectFromMobGriefWarZone;
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

        //Default 0
        return 0;
    }

    public boolean shouldNotifyWhenFactionRemoved()
    {
        return this._notifyWhenFactionRemoved;
    }

    public String getStorageType()
    {
        return _storageType;
    }

    public String getStorageUsername()
    {
        return _storageUserName;
    }

    public String getStoragePassword()
    {
        return _storagePassword;
    }

    public List<String> getWhiteListedItems()
    {
        return this._whitelistedItems;
    }

    public List<String> getWhiteListedPlaceDestroyBlocks()
    {
        return this._whitelistedPlaceDestroyBlocks;
    }

    public List<String> getWhiteListedInteractBlocks()
    {
        return this._whitelistedInteractBlocks;
    }

    public boolean shouldDisplayPvpLoggerInScoreboard()
    {
        return this._showPvpLoggerInScoreboard;
    }

    public boolean canUseFactionChest()
    {
        return this._canUseFactionChest;
    }

    public boolean shouldAllowExplosionsByOtherPlayersInClaims()
    {
        return this._allowExplosionsByOtherPlayersInClaims;
    }

    public boolean shouldProtectWarzoneFromPlayers()
    {
        return this._protectWarZoneFromPlayers;
    }

    public boolean shouldShowOnlyPlayerFactionsClaimsInMap()
    {
        return this._showOnlyPlayersFactionsClaimsInMap;
    }

    public String getDatabaseUrl()
    {
        return this._databaseUrl;
    }

    public String getDatabaseName()
    {
        return this._databaseFileName;
    }

    public boolean shouldSupressOtherFactionsMessagesWhileInTeamChat()
    {
        return this._supressOtherFactionsMessagesWhileInTeamChat;
    }
}
