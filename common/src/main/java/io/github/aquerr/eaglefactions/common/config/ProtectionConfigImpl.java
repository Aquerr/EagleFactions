package io.github.aquerr.eaglefactions.common.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ProtectionConfigImpl implements ProtectionConfig
{
	private final Configuration configuration;

	private boolean protectFromMobGrief = false;
	private boolean protectFromMobGriefWarZone = false;
	private boolean allowExplosionsByOtherPlayersInClaims = false;
	private boolean protectWarZoneFromPlayers = true;

	//Mob Spawning
	private boolean spawnMobsInSafeZone = true;
	private boolean spawnMobsInWarZone = true;
	private boolean spawnHostileMobsInWarZone = true;
	private boolean spawnMobsInFactionsTerritory = true;
	private boolean spawnHostileMobsInFactionsTerritory = true;

	//Worlds
	private Set<String> claimableWorldNames = new HashSet<>();
	private Set<String> notClaimableWorldNames = new HashSet<>();
	private Set<String> safezoneWorldNames = new HashSet<>();
	private Set<String> warzoneWorldNames = new HashSet<>();

	//Whitelisted items and blocks
	private Set<String> whitelistedItems = new HashSet<>();
	private Set<String> whitelistedPlaceDestroyBlocks = new HashSet<>();
	private Set<String> whitelistedInteractBlocks = new HashSet<>();

	public ProtectionConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public void reload()
	{
		this.protectFromMobGrief = this.configuration.getBoolean(false, "protect-from-mob-grief");
		this.protectFromMobGriefWarZone = this.configuration.getBoolean(false, "protect-from-mob-grief-warzone");
		this.allowExplosionsByOtherPlayersInClaims = this.configuration.getBoolean(false, "allow-explosions-by-other-players-in-claims");
		this.protectWarZoneFromPlayers = this.configuration.getBoolean(true, "protect-warzone-from-players");

		//Mob spawning
		this.spawnMobsInSafeZone = this.configuration.getBoolean(true, "spawn-mobs-in-safezone");
		this.spawnMobsInWarZone = this.configuration.getBoolean(true, "spawn-mobs-in-warzone");
		this.spawnHostileMobsInWarZone = this.configuration.getBoolean(true, "spawn-hostile-mobs-in-warzone");
		this.spawnMobsInFactionsTerritory = this.configuration.getBoolean(true, "spawn-mobs-in-factions-territory");
		this.spawnHostileMobsInFactionsTerritory = this.configuration.getBoolean(true, "spawn-hostile-mobs-in-factions-territory");

		//Worlds
		this.claimableWorldNames = new HashSet<>(this.configuration.getListOfStrings(new ArrayList<>(), "worlds", "CLAIMABLE"));
		this.notClaimableWorldNames = new HashSet<>(this.configuration.getListOfStrings(new ArrayList<>(), "worlds", "NOT_CLAIMABLE"));
		this.safezoneWorldNames = new HashSet<>(this.configuration.getListOfStrings(new ArrayList<>(), "worlds", "SAFE_ZONE"));
		this.warzoneWorldNames = new HashSet<>(this.configuration.getListOfStrings(new ArrayList<>(), "worlds", "WAR_ZONE"));

		//Whitelisted items and blocks
		this.whitelistedItems = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "items-whitelist");
		this.whitelistedPlaceDestroyBlocks = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "place-destroy-whitelist");
		this.whitelistedInteractBlocks = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "interact-whitelist");
	}

	@Override
	public Set<String> getClaimableWorldNames()
	{
		return this.claimableWorldNames;
	}

	@Override
	public Set<String> getNotClaimableWorldNames()
	{
		return this.notClaimableWorldNames;
	}

	@Override
	public Set<String> getSafeZoneWorldNames()
	{
		return this.safezoneWorldNames;
	}

	@Override
	public Set<String> getWarZoneWorldNames()
	{
		return this.warzoneWorldNames;
	}

	@Override
	public Set<String> getWhiteListedItems()
	{
		return this.whitelistedItems;
	}

	@Override
	public Set<String> getWhiteListedPlaceDestroyBlocks()
	{
		return this.whitelistedPlaceDestroyBlocks;
	}

	@Override
	public Set<String> getWhiteListedInteractBlocks()
	{
		return this.whitelistedInteractBlocks;
	}

	@Override
	public Set<String> getDetectedWorldNames()
	{
		final Set<String> detectedWorldNames = new HashSet<>();
		detectedWorldNames.addAll(getClaimableWorldNames());
		detectedWorldNames.addAll(getNotClaimableWorldNames());
		detectedWorldNames.addAll(getSafeZoneWorldNames());
		detectedWorldNames.addAll(getWarZoneWorldNames());
		return detectedWorldNames;
	}

	@Override
	public void addWorld(final String name)
	{
		this.claimableWorldNames.add(name);
		this.configuration.setCollectionOfStrings(claimableWorldNames, "worlds", "CLAIMABLE");
	}

	//Mob spawning methods
	@Override
	public boolean canSpawnMobsInSafeZone()
	{
		return this.spawnMobsInSafeZone;
	}

	@Override
	public boolean canSpawnMobsInWarZone()
	{
		return this.spawnMobsInWarZone;
	}

	@Override
	public boolean canSpawnHostileMobsInWarZone()
	{
		return this.spawnHostileMobsInWarZone;
	}

	@Override
	public boolean canSpawnMobsInFactionsTerritory()
	{
		return this.spawnMobsInFactionsTerritory;
	}

	@Override
	public boolean canSpawnHostileMobsInFactionsTerritory()
	{
		return this.spawnHostileMobsInFactionsTerritory;
	}

	@Override
	public boolean shouldProtectClaimFromMobGrief()
	{
		return protectFromMobGrief;
	}

	@Override
	public boolean shouldProtectWarZoneFromMobGrief()
	{
		return protectFromMobGriefWarZone;
	}

	@Override
	public boolean shouldAllowExplosionsByOtherPlayersInClaims()
	{
		return this.allowExplosionsByOtherPlayersInClaims;
	}

	@Override
	public boolean shouldProtectWarzoneFromPlayers()
	{
		return this.protectWarZoneFromPlayers;
	}
}
