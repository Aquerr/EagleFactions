package io.github.aquerr.eaglefactions.config;

import ninja.leaping.configurate.ConfigurationOptions;
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
public class Configuration implements IConfiguration
{
    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    private ConfigFields configFileds;

    public Configuration(Path configDir)
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

        if (!Files.exists(this.configPath))
        {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Settings.conf");
            try
            {
                Files.copy(inputStream, this.configPath);
                inputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            this.configLoader = HoconConfigurationLoader.builder().setPath(this.configPath).build();
            loadConfiguration();
        }
        else
        {
            this.configLoader = HoconConfigurationLoader.builder().setPath(this.configPath).build();
            loadConfiguration();
            save();
        }

        this.configFileds = new ConfigFields(this);
//        setup(configDir);
    }

    @Override
    public ConfigFields getConfigFileds()
    {
        return configFileds;
    }

    //    public void setup(Path configDir)
//    {
//    }

//    private void checkNodes()
//    {
//        Method[] methods = ConfigFields.class.getDeclaredMethods();
//        for (Method method: methods)
//        {
//            if (!method.getName().equals("setup") && !method.getName().equals("addWorld"))
//            {
//
//                try
//                {
//                    Object o = method.invoke(null);
//                }
//                catch (IllegalAccessException | InvocationTargetException e)
//                {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//    }

    @Override
    public void loadConfiguration()
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
            return (Double) value;
        }
        else return 0;
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
    public List<String> getListOfStrings(List<String> defaultValue, Object... nodePath)
    {
        return configNode.getNode(nodePath).getList(objectToStringTransformer, defaultValue);
    }

    @Override
    public boolean setListOfStrings(List<String> listOfStrings, Object... nodePath)
    {
        configNode.getNode(nodePath).setValue(listOfStrings);
        save();
        return true;
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
