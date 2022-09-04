package io.github.aquerr.eaglefactions.storage.file.hocon;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.vo.FactionName;
import io.github.aquerr.eaglefactions.storage.FactionStorage;
import io.github.aquerr.eaglefactions.util.FileUtils;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class HOCONFactionStorage implements FactionStorage
{
    private final Path configDir;
    private final Path factionsDir;

    private final Map<FactionName, ConfigurationLoader<CommentedConfigurationNode>> factionLoaders;

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
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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
                final HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder()
                        .defaultOptions(ConfigurateHelper.getDefaultOptions())
                        .path(path)
                        .build();
                factionLoaders.put(FactionName.of(factionFileName.toLowerCase().substring(0, factionFileName.lastIndexOf("."))), configurationLoader);
            });
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean saveFaction(final Faction faction)
    {
        try
        {
            FileUtils.createDirectoryIfNotExists(this.factionsDir);

            ConfigurationLoader<CommentedConfigurationNode> configurationLoader = this.factionLoaders.get(FactionName.of(faction.getName().toLowerCase()));

            if (configurationLoader == null)
            {
                final Path factionFilePath = this.factionsDir.resolve(faction.getName().toLowerCase() + ".conf");
                Files.createFile(factionFilePath);
                configurationLoader = HoconConfigurationLoader.builder().defaultOptions(ConfigurateHelper.getDefaultOptions()).path(factionFilePath).build();
                this.factionLoaders.put(FactionName.of(faction.getName().toLowerCase()), configurationLoader);
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

        this.factionLoaders.remove(FactionName.of(factionName.toLowerCase()));
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
        ConfigurationLoader<? extends ConfigurationNode> configurationLoader = this.factionLoaders.get(FactionName.of(factionName.toLowerCase()));
        if (configurationLoader == null)
        {
            final Path filePath = this.factionsDir.resolve(factionName.toLowerCase() + ".conf");

            // Check if file exists, if not then return null
            if (Files.notExists(filePath))
                return null;

            // Create configuration loader
            configurationLoader = HoconConfigurationLoader.builder().defaultOptions(ConfigurateHelper.getDefaultOptions()).path(filePath).build();
        }

        try
        {
            final ConfigurationNode configurationNode = configurationLoader.load();
            return ConfigurateHelper.getFactionFromNode(configurationNode);
        }
        catch (IOException e)
        {
            Sponge.server().sendMessage(Identity.nil(), text("Could not deserialize faction object from file! faction name = " + factionName, RED));
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
            return Collections.emptySet();
        }
    }

    @Override
    public void load()
    {
        loadFactionsConfigurationLoaders();
    }
}
