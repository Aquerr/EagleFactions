package io.github.aquerr.eaglefactions.common.config;

import com.google.common.reflect.TypeToken;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import io.github.aquerr.eaglefactions.api.config.*;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class ConfigurationImpl implements Configuration
{
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

    public ConfigurationImpl(final Path configDir, final Asset configAsset)
    {
        if (!Files.exists(configDir))
        {
            try
            {
                Files.createDirectory(configDir);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

        this.configPath = configDir.resolve("Settings.conf");

        try
        {
            configAsset.copyToFile(this.configPath, false, true);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        this.configLoader = HoconConfigurationLoader.builder().setFile(this.configPath.toFile()).build();
        loadConfiguration();
        save();

        this.storageConfig = new StorageConfigImpl(this);
        this.chatConfig = new ChatConfigImpl(this);
        this.dynmapConfig = new DynmapConfigImpl(this);
        this.powerConfig = new PowerConfigImpl(this);
        this.protectionConfig = new ProtectionConfigImpl(this);
        this.pvpLoggerConfig = new PVPLoggerConfigImpl(this);
        this.factionsConfig = new FactionsConfigImpl(this);
        reloadConfiguration();
        save();
    }

    @Override
    public FactionsConfig getFactionsConfig()
    {
        return this.factionsConfig;
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
            configNode = configLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
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
        return configNode.getNode(nodePath).getInt(defaultValue);
    }

    @Override
    public double getDouble(final double defaultValue, final Object... nodePath)
    {
        Object value = configNode.getNode(nodePath).getValue(defaultValue);

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
       return configNode.getNode(nodePath).getFloat(defaultValue);
    }

    @Override
    public boolean getBoolean(final boolean defaultValue, final Object... nodePath)
    {
        return configNode.getNode(nodePath).getBoolean(defaultValue);
    }

    @Override
    public String getString(final String defaultValue, final Object... nodePath)
    {
        return configNode.getNode(nodePath).getString(defaultValue);
    }

    @Override
    public List<String> getListOfStrings(final Collection<String> defaultValue, final Object... nodePath)
    {
        try
        {
            return configNode.getNode(nodePath).getList(TypeToken.of(String.class), new ArrayList<>(defaultValue));
        }
        catch(ObjectMappingException e)
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
            return new HashSet<>(configNode.getNode(nodePath).getList(TypeToken.of(String.class), new ArrayList<>(defaultValue)));
        }
        catch(ObjectMappingException e)
        {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    @Override
    public boolean setCollectionOfStrings(final Collection<String> collection, final Object... nodePath)
    {
        configNode.getNode(nodePath).setValue(collection);
        save();
        return true;
    }
}
