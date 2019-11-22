package io.github.aquerr.eaglefactions.common.config;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.dynmap.DynmapConfig;
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

    private ConfigFields configFields;


    //Configs
    private final DynmapConfig dynmapConfig;

    public ConfigurationImpl(final Path configDir, final Asset confgAsset)
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
            confgAsset.copyToFile(this.configPath, false, true);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        this.configLoader = HoconConfigurationLoader.builder().setPath(this.configPath).build();
        loadConfiguration();
//        save();

        this.configFields = new ConfigFields(this);
        this.dynmapConfig = new DynmapConfigImpl(this);
    }

    @Override
    public DynmapConfig getDynmapConfig()
    {
        return this.dynmapConfig;
    }

    @Override
    public ConfigFields getConfigFields()
    {
        return configFields;
    }

    @Override
    public void reloadConfiguration()
    {
        loadConfiguration();
        this.configFields.reload();
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
    public int getInt(int defaultValue, Object... nodePath)
    {
        return configNode.getNode(nodePath).getInt(defaultValue);
    }

    @Override
    public double getDouble(double defaultValue, Object... nodePath)
    {
        Object value = configNode.getNode(nodePath).getValue(defaultValue);

        if (value instanceof Integer)
        {
            int number = (Integer) value;
            return (double) number;
        }
        else if(value instanceof Double)
        {
            return (double) value;
        }
        else return 0;
    }

    @Override
    public float getFloat(float defaultValue, Object... nodePath)
    {
       return configNode.getNode(nodePath).getFloat(defaultValue);
    }

    @Override
    public boolean getBoolean(boolean defaultValue, Object... nodePath)
    {
        return configNode.getNode(nodePath).getBoolean(defaultValue);
    }

    @Override
    public String getString(String defaultValue, Object... nodePath)
    {
        return configNode.getNode(nodePath).getString(defaultValue);
    }

    @Override
    public List<String> getListOfStrings(Collection<String> defaultValue, Object... nodePath)
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
    public Set<String> getSetOfStrings(Collection<String> defaultValue, Object... nodePath)
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
    public boolean setListOfStrings(Collection<String> listOfStrings, Object... nodePath)
    {
        configNode.getNode(nodePath).setValue(listOfStrings);
        save();
        return true;
    }

    @Override
    public boolean setSetOfStrings(Collection<String> setOfStrings, Object... nodePath)
    {
        return false;
    }
}
