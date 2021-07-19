package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.*;
import io.github.aquerr.eaglefactions.storage.file.hocon.ConfigurateHelper;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class ConfigurationImpl implements Configuration
{
    private Path configDirectoryPath;
    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    //Configs
    private final StorageConfig storageConfig;
    private final ChatConfig chatConfig;
    private final DynmapConfig dynmapConfig;
    private final PowerConfig powerConfig;
    private final ProtectionConfig protectionConfig;
    private final PVPLoggerConfig pvpLoggerConfig;
    private final FactionsConfig factionsConfig;

    public ConfigurationImpl(final Path configDir, final Asset configAsset, final Asset worldsAsset)
    {
        this.configDirectoryPath = configDir;
        if (!Files.exists(this.configDirectoryPath))
        {
            try
            {
                Files.createDirectories(this.configDirectoryPath);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

        this.configPath = this.configDirectoryPath.resolve("Settings.conf");

        try
        {
            configAsset.copyToFile(this.configPath, false, true);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        this.configLoader = HoconConfigurationLoader.builder()
                .defaultOptions(ConfigurateHelper.getDefaultOptions())
                .path(this.configPath)
                .build();
        loadConfiguration();

        this.storageConfig = new StorageConfigImpl(this);
        this.chatConfig = new ChatConfigImpl(this);
        this.dynmapConfig = new DynmapConfigImpl(this);
        this.powerConfig = new PowerConfigImpl(this);
        this.protectionConfig = new ProtectionConfigImpl(this, worldsAsset);
        this.pvpLoggerConfig = new PVPLoggerConfigImpl(this);
        this.factionsConfig = new FactionsConfigImpl(this);
        reloadConfiguration();
    }

    @Override
    public FactionsConfig getFactionsConfig()
    {
        return this.factionsConfig;
    }

    @Override
    public Path getConfigDirectoryPath() {
        return this.configDirectoryPath;
    }

    @Override
    public Path getConfigPath() {
        return this.configPath;
    }

    @Override
    public ChatConfig getChatConfig()
    {
        return this.chatConfig;
    }

    @Override
    public DynmapConfig getDynmapConfig()
    {
        return this.dynmapConfig;
    }

    @Override
    public StorageConfig getStorageConfig()
    {
        return this.storageConfig;
    }

    @Override
    public PowerConfig getPowerConfig()
    {
        return this.powerConfig;
    }

    @Override
    public ProtectionConfig getProtectionConfig()
    {
        return this.protectionConfig;
    }

    @Override
    public PVPLoggerConfig getPvpLoggerConfig()
    {
        return this.pvpLoggerConfig;
    }

    @Override
    public void reloadConfiguration()
    {
        loadConfiguration();
        this.storageConfig.reload();
        this.chatConfig.reload();
        this.dynmapConfig.reload();
        this.powerConfig.reload();
        this.protectionConfig.reload();
        this.pvpLoggerConfig.reload();
        this.factionsConfig.reload();
    }

    private void loadConfiguration()
    {
        try
        {
            configNode = configLoader.load(ConfigurateHelper.getDefaultOptions()
                    .shouldCopyDefaults(true));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void save()
    {
        try
        {
            configLoader.save(configNode);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int getInt(final int defaultValue, final Object... nodePath)
    {
        return configNode.node(nodePath).getInt(defaultValue);
    }

    @Override
    public double getDouble(final double defaultValue, final Object... nodePath)
    {
        Object value = configNode.node(nodePath).getDouble(defaultValue);

        if (value instanceof Integer)
        {
            int number = (Integer) value;
            return number;
        }
        else if(value instanceof Double)
        {
            return (double) value;
        }
        else return 0;
    }

    @Override
    public float getFloat(final float defaultValue, final Object... nodePath)
    {
       return configNode.node(nodePath).getFloat(defaultValue);
    }

    @Override
    public boolean getBoolean(final boolean defaultValue, final Object... nodePath)
    {
        return configNode.node(nodePath).getBoolean(defaultValue);
    }

    @Override
    public String getString(final String defaultValue, final Object... nodePath)
    {
        return configNode.node(nodePath).getString(defaultValue);
    }

    @Override
    public List<String> getListOfStrings(final Collection<String> defaultValue, final Object... nodePath)
    {
        try
        {
            return configNode.node(nodePath).getList(String.class, new ArrayList<>(defaultValue));
        }
        catch(SerializationException e)
        {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Set<String> getSetOfStrings(final Collection<String> defaultValue,final Object... nodePath)
    {
        try
        {
            return new HashSet<>(configNode.node(nodePath).getList(String.class, new ArrayList<>(defaultValue)));
        }
        catch(SerializationException e)
        {
            e.printStackTrace();
        }
        return new HashSet<>();
    }
}
