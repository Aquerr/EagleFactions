package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.util.resource.Resource;
import io.github.aquerr.eaglefactions.util.resource.ResourceUtils;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class ProtectionConfigImpl implements ProtectionConfig
{
	private final Configuration configuration;

	private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
	private CommentedConfigurationNode worldsConfigNode;

	private boolean protectWildernessFromPlayers = false;
	private boolean allowExplosionsByOtherPlayersInClaims = false;
	private boolean protectWarZoneFromPlayers = true;

	//Worlds
	private Set<String> claimableWorldNames = new HashSet<>();
	private Set<String> notClaimableWorldNames = new HashSet<>();
	private Set<String> safeZoneWorldNames = new HashSet<>();
	private Set<String> warZoneWorldNames = new HashSet<>();

	private WhiteList safeZoneWhiteLists = null;
	private WhiteList warZoneWhiteLists = null;
	private WhiteList factionWhiteLists = null;
	private WhiteList wildernessWhiteLists = null;

	private List<String> blockedCommandsInOtherFactionsTerritory = new ArrayList<>();

	public ProtectionConfigImpl(final PluginContainer pluginContainer, final Configuration configuration)
	{
		this.configuration = configuration;

		try
		{
			Optional<Resource> worldResource = Optional.ofNullable(ResourceUtils.getResource("assets/eaglefactions/Worlds.conf"));
			if (!worldResource.isPresent())
			{
				throw new IllegalStateException("Could not open World.conf file which is required for plugin to work!");
			}

			Path worldsFilePath = configuration.getConfigDirectoryPath().resolve("Worlds.conf");
			if (Files.notExists(worldsFilePath))
			{
				Files.copy(worldResource.get().getInputStream(), worldsFilePath);
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}

		this.configurationLoader = HoconConfigurationLoader.builder().path(configuration.getConfigDirectoryPath().resolve("Worlds.conf")).build();
		loadWorldsFile();
		saveWorldsFile();
	}

	@Override
	public void reload() throws IOException
	{
		loadWorldsFile();

		this.protectWildernessFromPlayers = this.configuration.getBoolean(false, "protect-wilderness-from-players");
		this.allowExplosionsByOtherPlayersInClaims = this.configuration.getBoolean(false, "allow-explosions-by-other-players-in-claims");
		this.protectWarZoneFromPlayers = this.configuration.getBoolean(true, "protect-warzone-from-players");

		//Worlds
		this.claimableWorldNames = new HashSet<>(this.worldsConfigNode.node("worlds", "CLAIMABLE").getList(TypeToken.get(String.class), ArrayList::new));
		this.notClaimableWorldNames = new HashSet<>(this.worldsConfigNode.node("worlds", "NOT_CLAIMABLE").getList(TypeToken.get(String.class), ArrayList::new));
		this.safeZoneWorldNames = new HashSet<>(this.worldsConfigNode.node("worlds", "SAFE_ZONE").getList(TypeToken.get(String.class), ArrayList::new));
		this.warZoneWorldNames = new HashSet<>(this.worldsConfigNode.node("worlds", "WAR_ZONE").getList(TypeToken.get(String.class), ArrayList::new));
		validateWorlds();

		//Whitelisted items and blocks
		final Set<String> factionItemsWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "normal-faction", "items-whitelist");
		final Set<String> factionPlaceDestroyWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "normal-faction", "place-destroy-whitelist");
		final Set<String> factionInteractWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "normal-faction", "interact-whitelist");
		this.factionWhiteLists = new WhiteListsImpl(factionItemsWhiteList, factionInteractWhiteList, factionPlaceDestroyWhiteList);

		final Set<String> safeZoneItemsWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "safe-zone", "items-whitelist");
		final Set<String> safeZonePlaceDestroyWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "safe-zone", "place-destroy-whitelist");
		final Set<String> safeZoneInteractWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "safe-zone", "interact-whitelist");
		this.safeZoneWhiteLists = new WhiteListsImpl(safeZoneItemsWhiteList, safeZoneInteractWhiteList, safeZonePlaceDestroyWhiteList);

		final Set<String> warZoneItemsWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "war-zone", "items-whitelist");
		final Set<String> warZonePlaceDestroyWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "war-zone", "place-destroy-whitelist");
		final Set<String> warZoneInteractWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "war-zone", "interact-whitelist");
		this.warZoneWhiteLists = new WhiteListsImpl(warZoneItemsWhiteList, warZoneInteractWhiteList, warZonePlaceDestroyWhiteList);

		final Set<String> wildernessItemsWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "wilderness", "items-whitelist");
		final Set<String> wildernessPlaceDestroyWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "wilderness", "place-destroy-whitelist");
		final Set<String> wildernessInteractWhiteList = this.configuration.getSetOfStrings(new HashSet<>(), "allowed-items-and-blocks", "wilderness", "interact-whitelist");
		this.wildernessWhiteLists = new WhiteListsImpl(wildernessItemsWhiteList, wildernessInteractWhiteList, wildernessPlaceDestroyWhiteList);

		this.blockedCommandsInOtherFactionsTerritory = this.configuration.getListOfStrings(new ArrayList<>(), "blocked-commands-in-other-faction-territory");
	}

	private void validateWorlds()
	{
		// World name should exist only in one list.
		final List<String> worldNames = new ArrayList<>();
		worldNames.addAll(this.claimableWorldNames);
		worldNames.addAll(this.notClaimableWorldNames);
		worldNames.addAll(this.safeZoneWorldNames);
		worldNames.addAll(this.warZoneWorldNames);

		final List<String> worldNamesThatOccurredMoreThanOnce = worldNames.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		if (!worldNamesThatOccurredMoreThanOnce.isEmpty())
		{
			throw new IllegalStateException("Error processing Worlds.conf file. World name must exist only once and in only one list! " +
					"Worlds that exists multiple times: " + String.join(",", worldNamesThatOccurredMoreThanOnce));
		}
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
	public WhiteList getFactionWhitelists()
	{
		return this.factionWhiteLists;
	}

	@Override
	public WhiteList getSafeZoneWhitelists()
	{
		return this.safeZoneWhiteLists;
	}

	@Override
	public WhiteList getWarZoneWhitelists()
	{
		return this.warZoneWhiteLists;
	}

	@Override
	public WhiteList getWildernessWhitelists()
	{
		return this.wildernessWhiteLists;
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
		try
		{
			this.worldsConfigNode.node("worlds", "CLAIMABLE").set(this.claimableWorldNames);
		}
		catch (SerializationException e)
		{
			e.printStackTrace();
		}
		saveWorldsFile();
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
			this.worldsConfigNode = this.configurationLoader.load(ConfigurationOptions.defaults().shouldCopyDefaults(true));
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

	@Override
	public List<String> getBlockedCommandsInOtherFactionsTerritory()
	{
		return this.blockedCommandsInOtherFactionsTerritory;
	}

	public static final class WhiteListsImpl implements WhiteList
	{
		private Set<String> whitelistedItems;
		private Set<String> whitelistedPlaceDestroyBlocks;
		private Set<String> whitelistedInteractBlocks;

		protected WhiteListsImpl(final Set<String> whitelistedItems, final Set<String> whitelistedInteractBlocks, final Set<String> whitelistedPlaceDestroyBlocks)
		{
			this.whitelistedInteractBlocks = whitelistedInteractBlocks;
			this.whitelistedItems = whitelistedItems;
			this.whitelistedPlaceDestroyBlocks = whitelistedPlaceDestroyBlocks;
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
		public boolean isItemWhiteListed(String itemId)
		{
			return isWhiteListed(this.getWhiteListedItems(), itemId);
		}

		@Override
		public boolean isBlockWhitelistedForPlaceDestroy(String blockId)
		{
			return isWhiteListed(this.getWhiteListedPlaceDestroyBlocks(), blockId);
		}

		@Override
		public boolean isBlockWhiteListedForInteraction(String blockId)
		{
			return isWhiteListed(this.getWhiteListedInteractBlocks(), blockId);
		}

		private boolean isWhiteListed(final Collection<String> collection, final String id)
		{
			for(final String whiteListedIdPattern : collection)
			{
				if(whiteListedIdPattern.equals(id))
					return true;

				try
				{
					final Pattern pattern = Pattern.compile(whiteListedIdPattern);
					if(pattern.matcher(id).matches())
						return true;
				}
				catch(final PatternSyntaxException exception)
				{
					Sponge.server().sendMessage(Identity.nil(), Component.text("The syntax of your pattern is wrong. Id = " + whiteListedIdPattern, RED));
				}
			}
			return false;
		}
	}
}
