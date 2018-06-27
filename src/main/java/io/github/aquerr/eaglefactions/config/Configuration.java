package io.github.aquerr.eaglefactions.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Aquerr on 2017-07-12.
 */
@Singleton
public class Configuration
{
    private static Function<Object, String> objectToStringTransformer = input ->
    {
        if (input instanceof String)
        {
            return (String) input;
        } else
        {
            return null;
        }
    };
    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    @Inject
    public Configuration(@Named("config dir") Path configPath)
    {
        setup(configPath);
    }

    public void setup(Path configDir)
    {
        if (!Files.exists(configDir))
        {
            try
            {
                Files.createDirectory(configDir);
            } catch (IOException exception)
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
                inputStream.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            configLoader = HoconConfigurationLoader.builder().setPath(configPath).build();
            load();
        } else
        {
            configLoader = HoconConfigurationLoader.builder().setPath(configPath).build();
            load();
            checkNodes();
            save();
        }
    }

    private void checkNodes()
    {
        Method[] methods = Settings.class.getDeclaredMethods();
        for (Method method : methods)
        {
            if (!method.getName().equals("setup") && !method.getName().equals("addWorld"))
            {

                try
                {
                    Object o = method.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    public void load()
    {
        try
        {
            configNode = configLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            //settings.setup(this);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void save()
    {
        try
        {
            configLoader.save(configNode);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int getInt(int defaultValue, Object... nodePath)
    {
        return configNode.getNode(nodePath).getInt(defaultValue);
    }

    public double getDouble(double defaultValue, Object... nodePath)
    {
        Object value = configNode.getNode(nodePath).getValue(defaultValue);

        if (value instanceof Integer)
        {
            int number = (Integer) value;
            return (double) number;
        } else if (value instanceof Double)
        {
            return (Double) value;
        } else return 0;
    }

    public boolean getBoolean(boolean defaultValue, Object... nodePath)
    {
        return configNode.getNode(nodePath).getBoolean(defaultValue);
    }

    public String getString(String defaultValue, Object... nodePath)
    {
        return configNode.getNode(nodePath).getString(defaultValue);
    }

    public List<String> getListOfStrings(List<String> defaultValue, Object... nodePath)
    {
        return configNode.getNode(nodePath).getList(objectToStringTransformer, defaultValue);
    }

    public boolean setListOfStrings(List<String> listOfStrings, Object... nodePath)
    {
        configNode.getNode(nodePath).setValue(listOfStrings);
        save();
        return true;
    }
}
