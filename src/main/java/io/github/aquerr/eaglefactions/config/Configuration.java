package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.logic.MainLogic;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class Configuration
{
    //TODO: This class should have only one instance. Rework it to singleton.

    public Configuration(Path configDir)
    {
        setup(configDir);
    }

    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    public void setup(Path configDir)
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

        configPath = configDir.resolve("Settings.conf");

        if (!Files.exists(configPath))
        {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Settings.conf");
            try
            {
                Files.copy(inputStream, configPath);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        configLoader = HoconConfigurationLoader.builder().setPath(configPath).build();

        load();
    }

    public void load()
    {
        try
        {
            configNode = configLoader.load();
            MainLogic.setup(this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

//    public void save()
//    {
//        try
//        {
//            configLoader.save(configNode);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }

//    public Path getConfigPath()
//    {
//        return configPath;
//    }

    public int getInt(Object... nodePath)
    {
        return configNode.getNode(nodePath).getInt();
    }

    public double getDouble(Object... nodePath)
    {
        Object value = configNode.getNode(nodePath).getValue();

        if (value instanceof Integer)
        {
            int number = ((Integer) value).intValue();
            return (double) number;
        }
        else if(value instanceof Double)
        {
            return ((Double) value).doubleValue();
        }
        else return 0;
    }

    public boolean getBoolean(Object... nodePath)
    {
        return configNode.getNode(nodePath).getBoolean();
    }

    public String getString(Object... nodePath)
    {
        return configNode.getNode(nodePath).getString();
    }

    public List<String> getListOfStrings(Object... nodePath)
    {
        return configNode.getNode(nodePath).getList(objectToStringTransformer);
    }

    private static Function<Object,String> objectToStringTransformer = input ->
    {
        if (input instanceof String)
        {
            return (String) input;
        }
        else
        {
            return null;
        }
    };
}
