package io.github.aquerr.eaglefactions.common.storage.file.hocon;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.storage.FactionStorage;
import io.github.aquerr.eaglefactions.common.storage.serializers.ClaimTypeSerializer;
import io.github.aquerr.eaglefactions.common.util.FileUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HOCONFactionStorage implements FactionStorage
{
    private final Path configDir;
    private final Path factionsDir;

    // Faction name --> Configuration Loader
    //TODO: Maybe we should also store ConfigurationNodes here?
    private final Map<String, ConfigurationLoader<? extends ConfigurationNode>> factionLoaders;

    public HOCONFactionStorage(final Path configDir)
    {
        this.configDir = configDir;
        this.factionsDir = configDir.resolve("factions");
        this.factionLoaders = new HashMap<>();

        if (Files.notExists(this.factionsDir))
        {
            try
            {
                Files.createDirectory(this.factionsDir);
                preCreate();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        // Backwards compatibility with 0.14.x
        // Convert old file format to the new one.
        // This code will be removed in future releases.
        if (Files.exists(this.configDir.resolve("data")) && Files.exists(this.configDir.resolve("data").resolve("factions.conf")))
        {
            migrateOldFactionsDataToNewFormat();
        }

        loadFactionsConfigurationLoaders();
    }

    private void loadFactionsConfigurationLoaders()
    {
        try
        {
            final Stream<Path> pathsStream = Files.list(this.factionsDir);
            pathsStream.forEach(path ->
            {
                final String factionFileName = path.getFileName().toString().toLowerCase();
                final HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(this.factionsDir.resolve(path)).build();
                factionLoaders.put(factionFileName, configurationLoader);
            });
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private void migrateOldFactionsDataToNewFormat()
    {
        final Path oldFactionsFile = this.configDir.resolve("data").resolve("factions.conf");
        final ConfigurationLoader<CommentedConfigurationNode> configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurationOptions.defaults()).setPath(oldFactionsFile).build();
        try
        {
            final ConfigurationNode configNode = configurationLoader.load();
            final List<Faction> factions = ConfigurateHelper.getFactionsFromNode(configNode.getNode("factions"));
            final List<Faction> correctedFactions = new ArrayList<>();

            //Claims were stored differently before so we need to load them properly
            for (final Faction faction : factions)
            {
                final Set<Claim> updatedClaims = new HashSet<>();
                final Object claims = configNode.getNode("factions", faction.getName(), "claims").getValue();
                if (claims != null)
                {
                    final List<String> claimsAsStrings = (List<String>)claims;
                    for (final String claimAsString : claimsAsStrings)
                    {
                        final String[] worldAndChunk = claimAsString.split("\\|");
                        final String world = worldAndChunk[0];
                        final String chunk = worldAndChunk[1];
                        final UUID worldUUID = UUID.fromString(world);
                        final Vector3i chunkPosition = ClaimTypeSerializer.deserializeVector3i(chunk);
                        final Claim claim = new Claim(worldUUID, chunkPosition);
                        updatedClaims.add(claim);
                    }
                }
                final Faction updatedFaction = faction.toBuilder().setClaims(updatedClaims).build();
                correctedFactions.add(updatedFaction);
            }

            //Generate new factions files.
            for (final Faction faction : correctedFactions)
            {
                final Path factionFilePath = this.configDir.resolve("factions").resolve(faction.getName().toLowerCase() + ".conf");

                //Create faction file only if it not exists. We don't want to override the existing data.
                if (Files.notExists(factionFilePath))
                {
                    Files.createFile(factionFilePath);
                    final HoconConfigurationLoader hoconConfigurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(factionFilePath).build();
                    final ConfigurationNode node = hoconConfigurationLoader.load();
                    ConfigurateHelper.putFactionInNode(node, faction);
                    hoconConfigurationLoader.save(node);
                }
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private void preCreate()
    {
        if (!this.factionLoaders.containsKey("warzone.conf"))
        {
            final Faction warzone = FactionImpl.builder("WarZone", Text.of("WZ"), new UUID(0, 0)).build();
            saveFaction(warzone);
        }
        if (!this.factionLoaders.containsKey("safezone.conf"))
        {
            final Faction safezone = FactionImpl.builder("SafeZone", Text.of("SZ"), new UUID(0, 0)).build();
            saveFaction(safezone);
        }
    }

    @Override
    public boolean saveFaction(final Faction faction)
    {
        try
        {
            FileUtils.createDirectoryIfNotExists(this.factionsDir);

            ConfigurationLoader<? extends ConfigurationNode> configurationLoader = this.factionLoaders.get(faction.getName().toLowerCase() + ".conf");

            if (configurationLoader == null)
            {
                final Path factionFilePath = this.factionsDir.resolve(faction.getName().toLowerCase() + ".conf");
                Files.createFile(factionFilePath);
                configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(factionFilePath).build();
                this.factionLoaders.put(factionFilePath.getFileName().toString(), configurationLoader);
            }

            final ConfigurationNode configurationNode = configurationLoader.load();
            final boolean didSucceed = ConfigurateHelper.putFactionInNode(configurationNode, faction);
            if (didSucceed)
            {
                configurationLoader.save(configurationNode);
                return true;
            }
            else return false;
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteFaction(final String factionName)
    {
        final Path filePath = this.factionsDir.resolve(factionName.toLowerCase() + ".conf");
        try
        {
            Files.deleteIfExists(filePath);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        this.factionLoaders.remove(factionName.toLowerCase() + ".conf");
        return true;
    }

    @Override
    public void deleteFactions()
    {
        this.factionLoaders.clear();

        if (Files.notExists(this.factionsDir))
            return;

        try
        {
            Files.list(this.factionsDir).forEach(path ->
            {
                try
                {
                    Files.delete(path);
                }
                catch (final IOException e)
                {
                    e.printStackTrace();
                }
            });
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public @Nullable Faction getFaction(final String factionName)
    {
        ConfigurationLoader<? extends ConfigurationNode> configurationLoader = this.factionLoaders.get(factionName.toLowerCase() + ".conf");
        if (configurationLoader == null)
        {
            final Path filePath = this.factionsDir.resolve(factionName.toLowerCase() + ".conf");

            // Check if file exists, if not then return null
            if (Files.notExists(filePath))
                return null;

            // Create configuration loader
            configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(this.factionsDir.resolve(filePath)).build();
        }

        if (configurationLoader == null)
            return null;

        try
        {
            final ConfigurationNode configurationNode = configurationLoader.load();
            return ConfigurateHelper.getFactionFromNode(configurationNode);
        }
        catch (IOException | ObjectMappingException e)
        {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "Could not deserialize faction object from file! faction name = " + factionName));
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Faction> getFactions()
    {
        try
        {
            return Files.list(this.factionsDir)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        final String factionName = path.getFileName().toString().substring(0, path.getFileName().toString().lastIndexOf("."));
                        return getFaction(factionName);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return Collections.EMPTY_SET;
    }

    @Override
    public void load()
    {
        loadFactionsConfigurationLoaders();
    }
}
