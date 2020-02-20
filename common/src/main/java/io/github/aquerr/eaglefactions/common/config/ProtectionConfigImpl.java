package io.github.aquerr.eaglefactions.common.config;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ProtectionConfigImpl implements ProtectionConfig
{
	private final
	private final Configuration configuration;

	private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
	private CommentedConfigurationNode worldsConfigNode;

	private boolean protectWildernessFromPlayers = false;
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
	private Set<String> safeZoneWorldNames = new HashSet<>();
	private Set<String> warZoneWorldNames = new HashSet<>();

	//Whitelisted items and blocks
	private Set<String> whitelistedItems = new HashSet<>();
	private Set<String> whitelistedPlaceDestroyBlocks = new HashSet<>();
	private Set<String> whitelistedInteractBlocks = new HashSet<>();

	public ProtectionConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;

		try
		{
			Optional<Asset> worldsFile = Sponge.getAssetManager().getAsset(EagleFactionsPlugin.getPlugin(), "Worlds.conf");
			if (worldsFile.isPresent())
			{
				worldsFile.get().copyToFile(configuration.getConfigDirectoryPath().resolve("Worlds.conf"), false, true);
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}

		this.configurationLoader = HoconConfigurationLoader.builder().setPath(configuration.getConfigDirectoryPath().resolve("Worlds.conf")).build();
		saveWorldsFile();
	}

	@Override
	public void reload()
	{
		loadWorldsFile();

		this.protectWildernessFromPlayers = this.configuration.getBoolean(false, "protect-wilderness-from-players");
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
		try
		{
			this.claimableWorldNames = new HashSet<>(this.worldsConfigNode.getNode("worlds", "CLAIMABLE").getList(TypeToken.of(String.class), new ArrayList<>()));
			this.notClaimableWorldNames = new HashSet<>(this.worldsConfigNode.getNode("worlds", "NOT_CLAIMABLE").getList(TypeToken.of(String.class), new ArrayList<>()));
			this.safeZoneWorldNames = new HashSet<>(this.worldsConfigNode.getNode("worlds", "SAFE_ZONE").getList(TypeToken.of(String.class), new ArrayList<>()));
			this.warZoneWorldNames = new HashSet<>(this.worldsConfigNode.getNode("worlds", "WAR_ZONE").getList(TypeToken.of(String.class), new ArrayList<>()));
		}
		catch (final ObjectMappingException e)
		{
			e.printStackTrace();
		}

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
		return this.safeZoneWorldNames;
	}

	@Override
	public Set<String> getWarZoneWorldNames()
	{
		return this.warZoneWorldNames;
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
		this.worldsConfigNode.getNode("worlds", "CLAIMABLE").setValue(this.claimableWorldNames);
		saveWorldsFile();
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

	@Override
	public boolean shouldProtectWildernessFromPlayers()
	{
		return this.protectWildernessFromPlayers;
	}

	private void loadWorldsFile()
	{
		try
		{
			this.worldsConfigNode = this.configurationLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void saveWorldsFile()
	{
		try
		{
			this.configurationLoader.save(this.worldsConfigNode);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
