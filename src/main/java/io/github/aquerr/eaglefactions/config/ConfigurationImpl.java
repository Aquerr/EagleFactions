package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.BluemapConfig;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.ConfigReloadable;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.DynmapConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.HomeConfig;
import io.github.aquerr.eaglefactions.api.config.PVPLoggerConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.config.StorageConfig;
import io.github.aquerr.eaglefactions.api.config.VersionConfig;
import io.github.aquerr.eaglefactions.storage.file.hocon.ConfigurateHelper;
import io.github.aquerr.eaglefactions.util.FileUtils;
import io.github.aquerr.eaglefactions.util.resource.Resource;
import io.leangen.geantyref.TypeToken;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurationImpl implements Configuration
{
    private final Path configDirectoryPath;
    private final Path configPath;
    private final ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    private final Map<Class<? extends ConfigReloadable>, ConfigReloadable> configs = new HashMap<>();

    public ConfigurationImpl(final PluginContainer pluginContainer, final Path configDir, final Resource configAsset) throws IOException
    {
        this.configDirectoryPath = configDir;
        FileUtils.createDirectoryIfNotExists(this.configDirectoryPath);

        this.configPath = this.configDirectoryPath.resolve("Settings.conf");

        try
        {
            if (Files.notExists(this.configPath))
                Files.copy(configAsset.getInputStream(), this.configPath);
        }
        catch (final IOException e)
        {
            throw new IllegalStateException(e);
        }

        this.configLoader = (HoconConfigurationLoader.builder()).path(this.configPath).build();
        loadConfiguration();

        this.configs.put(StorageConfig.class, new StorageConfigImpl(this));
        this.configs.put(ChatConfig.class, new ChatConfigImpl(this));
        this.configs.put(DynmapConfig.class, new DynmapConfigImpl(this));
        this.configs.put(PowerConfig.class, new PowerConfigImpl(this));
        this.configs.put(ProtectionConfig.class, new ProtectionConfigImpl(pluginContainer, this));
        this.configs.put(PVPLoggerConfig.class, new PVPLoggerConfigImpl(this));
        this.configs.put(FactionsConfig.class, new FactionsConfigImpl(this));
        this.configs.put(BluemapConfig.class, new BluemapConfigImpl(this));
        this.configs.put(HomeConfig.class, new HomeConfigImpl(this));
        this.configs.put(VersionConfig.class, new VersionConfigImpl(this));
        reloadConfiguration();
    }

    @Override
    public FactionsConfig getFactionsConfig()
    {
        return getConfig(FactionsConfig.class);
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
        return getConfig(ChatConfig.class);
    }

    @Override
    public DynmapConfig getDynmapConfig()
    {
        return getConfig(DynmapConfig.class);
    }

    @Override
    public StorageConfig getStorageConfig()
    {
        return getConfig(StorageConfig.class);
    }

    @Override
    public BluemapConfig getBluemapConfig()
    {
        return getConfig(BluemapConfig.class);
    }

    @Override
    public PowerConfig getPowerConfig()
    {
        return getConfig(PowerConfig.class);
    }

    @Override
    public ProtectionConfig getProtectionConfig()
    {
        return getConfig(ProtectionConfig.class);
    }

    @Override
    public PVPLoggerConfig getPvpLoggerConfig()
    {
        return getConfig(PVPLoggerConfig.class);
    }

    @Override
    public HomeConfig getHomeConfig()
    {
        return getConfig(HomeConfig.class);
    }

    @Override
    public VersionConfig getVersionConfig()
    {
        return getConfig(VersionConfig.class);
    }

    @Override
    public void reloadConfiguration() throws IOException
    {
        loadConfiguration();
        for (ConfigReloadable value : this.configs.values())
        {
            value.reload();
        }
    }

    private void loadConfiguration() throws IOException
    {
        configNode = configLoader.load(ConfigurateHelper.getDefaultOptions().shouldCopyDefaults(true));
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
        return configNode.node(nodePath).getDouble(defaultValue);
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
            return configNode.node(nodePath).getList(TypeToken.get(String.class), () -> new ArrayList<>(defaultValue));
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
            return new HashSet<>(configNode.node(nodePath).getList(TypeToken.get(String.class), new ArrayList<>(defaultValue)));
        }
        catch(SerializationException e)
        {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    @Override
    public <T> List<T> getGenericList(Class<T> clazz, final Collection<T> defaultValue, final Object... nodePath)
    {
        try
        {
            return configNode.node(nodePath).getList(TypeToken.get(clazz), () -> new ArrayList<>(defaultValue));
        }
        catch(SerializationException e)
        {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private <T> T getConfig(Class<T> clazz)
    {
        return (T)this.configs.get(clazz);
    }
}
