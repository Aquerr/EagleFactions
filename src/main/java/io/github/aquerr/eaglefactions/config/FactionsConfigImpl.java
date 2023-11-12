package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactionsConfigImpl implements FactionsConfig
{
	private final Configuration configuration;

	//TODO: Maybe move to GeneralConfig class?
	//TODO: This should be possibly changed to .lang file.
	private String languageTag = "en";

	private int maxNameLength = 30;
	private int minNameLength = 3;
	private int maxTagLength = 5;
	private int minTagLength = 2;

	private boolean isPlayerLimit = false;
	private int playerLimit = 15;
	private int attackTime = 10;
	private float percentageDamageReductionInOwnTerritory = 10.0f;

	private boolean isFactionFriendlyFire = false;
	private boolean isTruceFriendlyFire = true;
	private boolean isAllianceFriendlyFire = false;

	private boolean requireConnectedClaims = true;
	private boolean shouldDelayClaim = false;
	private int claimDelay = 10;
	private boolean claimByItems = false;
	private Map<String, Integer> requiredItemsToClaim = new HashMap<>();

	private boolean canUseFactionChest = true;

	private boolean factionCreationByItems = false;
	private Map<String, Integer> requiredItemsToCreateFaction = new HashMap<>();

	private boolean blockEnteringOfflineFactions = false;
	private boolean blockEnteringSafezoneFromWarzone = false;
	private boolean canAttackOnlyAtNight = false;
	private String maxInactiveTime = "0";
	private boolean notifyWhenFactionRemoved = true;
	private boolean notifyWhenFactionCreated = false;
	private boolean regenerateChunksWhenFactionRemoved = false;
	private boolean showOnlyPlayersFactionsClaimsInMap = false;

	private boolean shouldInformAboutAttack = true;
	private boolean shouldShowAttackedClaim = true;
	private boolean shouldInformAboutDestroy = true;
	private boolean shouldShowDestroyedClaim = true;
	private boolean shouldShowAttackInBossBar = true;

	public FactionsConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public void reload()
	{
		this.languageTag = this.configuration.getString("en", "language");

		this.maxNameLength = this.configuration.getInt(30,"name", "max-length");
		this.minNameLength = this.configuration.getInt(3, "name", "min-length");
		this.maxTagLength = this.configuration.getInt(5, "tag", "max-length");
		this.minTagLength = this.configuration.getInt(2, "tag", "min-length");

		this.isPlayerLimit = this.configuration.getBoolean(false, "player-limit", "toggled");
		this.playerLimit = this.configuration.getInt(15, "player-limit", "limit");
		this.attackTime = this.configuration.getInt(10, "attack-time");
		this.percentageDamageReductionInOwnTerritory = this.configuration.getFloat(10.0f, "percentage-damage-reduction-in-own-territory");

		this.isFactionFriendlyFire = this.configuration.getBoolean(false, "friendlyfire-faction");
		this.isTruceFriendlyFire = this.configuration.getBoolean(true, "friendlyfire-truce");
		this.isAllianceFriendlyFire = this.configuration.getBoolean(false, "friendlyfire-alliance");

		this.requireConnectedClaims = this.configuration.getBoolean(true, "connected-claims");
		this.shouldDelayClaim = this.configuration.getBoolean(false, "delayed-claim", "toggled");
		this.claimDelay = this.configuration.getInt(10, "delayed-claim", "claiming-time");
		this.claimByItems = this.configuration.getBoolean(false, "claiming-by-items", "toggled");
		this.requiredItemsToClaim = prepareItems(this.configuration.getListOfStrings(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20", "minecraft:iron_ingot|4"), "claiming-by-items", "items"));

		this.canUseFactionChest = this.configuration.getBoolean(true, "faction-chest");

		this.factionCreationByItems = this.configuration.getBoolean(false, "creating-by-items", "toggled");
		this.requiredItemsToCreateFaction = prepareItems(this.configuration.getListOfStrings(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20"), "creating-by-items", "items"));

		this.blockEnteringOfflineFactions = this.configuration.getBoolean(true, "block-entering-faction-while-offline");
		this.blockEnteringSafezoneFromWarzone = this.configuration.getBoolean(false, "block-safezone-from-warzone");

		this.canAttackOnlyAtNight = this.configuration.getBoolean(false, "attack-only-at-night");

		this.maxInactiveTime = this.configuration.getString("30d", "factions-remover", "max-inactive-time");
		this.notifyWhenFactionRemoved = this.configuration.getBoolean(true, "factions-remover", "notify-when-removed");
		this.notifyWhenFactionCreated = this.configuration.getBoolean(false, "should-notify-when-faction-created");
		this.regenerateChunksWhenFactionRemoved = this.configuration.getBoolean(false, "factions-remover", "regenerate-when-removed");
		this.showOnlyPlayersFactionsClaimsInMap = this.configuration.getBoolean(false, "show-only-player-faction-claims-in-map");

		this.shouldInformAboutAttack = this.configuration.getBoolean(true, "inform-about-attack");
		this.shouldShowAttackedClaim = this.configuration.getBoolean(true, "show-attacked-claim");
		this.shouldInformAboutDestroy = this.configuration.getBoolean(true, "inform-about-destroy");
		this.shouldShowDestroyedClaim = this.configuration.getBoolean(true, "show-destroyed-claim");
		this.shouldShowAttackInBossBar = this.configuration.getBoolean(true, "show-attack-in-bossbar");
	}

	@Override
	public String getLanguageTag()
	{
		return this.languageTag;
	}

	@Override
	public int getMaxNameLength()
	{
		return this.maxNameLength;
	}

	@Override
	public int getMinNameLength()
	{
		return this.minNameLength;
	}

	@Override
	public int getMaxTagLength()
	{
		return this.maxTagLength;
	}

	@Override
	public int getMinTagLength()
	{
		return this.minTagLength;
	}

	@Override
	public boolean isPlayerLimit()
	{
		return this.isPlayerLimit;
	}

	@Override
	public int getPlayerLimit()
	{
		return this.playerLimit;
	}

	@Override
	public int getAttackTime()
	{
		return this.attackTime;
	}

	@Override
	public float getPercentageDamageReductionInOwnTerritory()
	{
		return this.percentageDamageReductionInOwnTerritory;
	}

	@Override
	public boolean isFactionFriendlyFire()
	{
		return this.isFactionFriendlyFire;
	}

	@Override
	public boolean isTruceFriendlyFire()
	{
		return this.isTruceFriendlyFire;
	}

	@Override
	public boolean isAllianceFriendlyFire()
	{
		return this.isAllianceFriendlyFire;
	}

	@Override
	public boolean shouldDelayClaim()
	{
		return this.shouldDelayClaim;
	}

	@Override
	public int getClaimDelay()
	{
		return this.claimDelay;
	}

	@Override
	public boolean shouldClaimByItems()
	{
		return this.claimByItems;
	}

	@Override
	public Map<String, Integer> getRequiredItemsToClaim()
	{
		return this.requiredItemsToClaim;
	}

	@Override
	public boolean canUseFactionChest()
	{
		return this.canUseFactionChest;
	}

	@Override
	public boolean requireConnectedClaims()
	{
		return this.requireConnectedClaims;
	}

	@Override
	public boolean getFactionCreationByItems()
	{
		return this.factionCreationByItems;
	}

	@Override
	public Map<String, Integer> getRequiredItemsToCreateFaction()
	{
		return this.requiredItemsToCreateFaction;
	}

	@Override
	public boolean getBlockEnteringFactions()
	{
		return this.blockEnteringOfflineFactions;
	}

	@Override
	public boolean shouldBlockEnteringSafezoneFromWarzone()
	{
		return this.blockEnteringSafezoneFromWarzone;
	}

	@Override
	public boolean canAttackOnlyAtNight()
	{
		return this.canAttackOnlyAtNight;
	}

	@Override
	public long getMaxInactiveTime()
	{
		char lastCharacter = this.maxInactiveTime.charAt(this.maxInactiveTime.length() - 1);

		if(this.maxInactiveTime.charAt(0) == '0')
		{
			return 0;
		}
		else if('d' == lastCharacter || 'D' == lastCharacter)
		{
			return Long.parseLong(this.maxInactiveTime.substring(0, this.maxInactiveTime.length() - 1)) * 24 * 60 * 60;
		}
		else if('h' == lastCharacter || 'H' == lastCharacter)
		{
			return Long.parseLong(this.maxInactiveTime.substring(0, this.maxInactiveTime.length() - 1)) * 60 * 60;
		}
		else if('m' == lastCharacter || 'M' == lastCharacter)
		{
			return Long.parseLong(this.maxInactiveTime.substring(0, this.maxInactiveTime.length() - 1)) * 60;
		}
		else if('s' == lastCharacter || 'S' == lastCharacter)
		{
			return Long.parseLong(this.maxInactiveTime.substring(0, this.maxInactiveTime.length() - 1));
		}

		//Default 0
		return 0;
	}

	@Override
	public boolean shouldNotifyWhenFactionRemoved()
	{
		return this.notifyWhenFactionRemoved;
	}

	@Override
	public boolean shouldNotifyWHenFactionCreated()
	{
		return this.notifyWhenFactionCreated;
	}

	@Override
	public boolean shouldRegenerateChunksWhenFactionRemoved()
	{
		return this.regenerateChunksWhenFactionRemoved;
	}

	@Override
	public boolean shouldShowOnlyPlayerFactionsClaimsInMap()
	{
		return this.showOnlyPlayersFactionsClaimsInMap;
	}

	@Override
	public boolean shouldInformAboutAttack()
	{
		return this.shouldInformAboutAttack;
	}

	@Override
	public boolean shouldInformAboutDestroy()
	{
		return this.shouldInformAboutDestroy;
	}

	@Override
	public boolean shouldShowAttackedClaim()
	{
		return this.shouldShowAttackedClaim;
	}

	@Override
	public boolean shouldShowDestroyedClaim()
	{
		return this.shouldShowDestroyedClaim;
	}

	@Override
	public boolean shouldShowAttackInBossBar()
	{
		return this.shouldShowAttackInBossBar;
	}

	private HashMap<String, Integer> prepareItems(final List<String> itemsToPrepare)
	{
		final HashMap<String, Integer> items = new HashMap<>();
		for (final String itemWithAmount : itemsToPrepare)
		{
			final String[] strings = itemWithAmount.split("\\|");
			final String item = strings[0];
			final int amount = Integer.parseInt(strings[1]);
			items.put(item, amount);
		}
		return items;
	}
}
