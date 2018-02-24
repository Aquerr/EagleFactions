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
public class MainConfig
{
    private MainConfig _mainConfig = new MainConfig();

    public MainConfig()
    {

    }

    private static Path configPath;
    private static ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private static CommentedConfigurationNode configNode;

    public static void setup(Path configDir)
    {
        if (!Files.exists(configDir))
        {
            try
            {
                Files.createDirectory(configDir);
                configPath = configDir.resolve("Settings.conf");
                configLoader = HoconConfigurationLoader.builder().setPath(configPath).build();

                InputStream inputStream = MainConfig.class.getClass().getClassLoader().getResourceAsStream("Settings.conf");
                Files.copy(inputStream, configPath);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }

        load();
        setupMainLogic(configPath);
    }

    private static void setupMainLogic(Path configPath)
    {
        //TODO: Try to send the config class to main logic.
        MainLogic.setup(configPath);
    }

    public static void load()
    {
        try
        {
            configNode = configLoader.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void save()
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

    public static CommentedConfigurationNode get()
    {
        return configNode;
    }
}
