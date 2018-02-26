package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.logic.MainLogic;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class Configuration
{
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
        setupMainLogic(this);
    }

    private void setupMainLogic(Configuration configuration)
    {
        //TODO: Try to send the config class to main logic.
        MainLogic.setup(configuration);
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

    public CommentedConfigurationNode get()
    {
        return configNode;
    }

    public Path getConfigPath()
    {
        return configPath;
    }
}
