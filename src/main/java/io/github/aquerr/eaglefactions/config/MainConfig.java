package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.EagleFactions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class MainConfig implements IConfig
{
    private static MainConfig mainConfig = new MainConfig();

    private MainConfig()
    {
        ;
    }

    public static MainConfig getConfig()
    {
        return mainConfig;
    }

    private Path configFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir() + "/Settings.conf");
    private ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
    private CommentedConfigurationNode configNode;

    @Override
    public void setup()
    {
        if (! Files.exists(configFile))
        {
            try
            {
                Files.createFile(configFile);
                load();
                populate();
                save();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            load();
        }
    }

    @Override
    public void load()
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
    public void populate()
    {
        get().getNode("eaglefactions").setComment("Contains all Eagle Factions related settings.");
        get().getNode("eaglefactions", "name", "maxlength").setValue(30).setComment("This determines the maximum amount of characters a Factions's name can be.");
        get().getNode("eaglefactions", "name", "minlength").setValue(3).setComment("This determines the minimum amount of characters a Factions's name can be.");
        get().getNode("eaglefactions", "tag", "maxlength").setValue(5).setComment("This determines the minimum amount of characters a Factions's tag can be.");
        get().getNode("eaglefactions", "tag", "minlength").setValue(2).setComment("This determines the minimum amount of characters a Factions's tag can be.");
        get().getNode("eaglefactions", "prefix", "display").setValue(true).setComment("Allows/denies displaying Faction prefixes.");
        get().getNode("eaglefactions", "power", "increment").setValue(0.04).setComment("How much power will be restored for player after 1 minute of playing. (0.04 per minute = 1,2 per hour");
        get().getNode("eaglefactions", "power", "decrement").setValue(2.0).setComment("How much power will be removed on player death");
        get().getNode("eaglefactions", "power", "maxpower").setValue(10.0).setComment("Maximum amount of power a player can have");
        get().getNode("eaglefactions", "power", "startpower").setValue(5.0).setComment("Starting amount of power");
        get().getNode("eaglefactions", "power", "killaward").setValue(2.0).setComment("Player kill award");
        get().getNode("eaglefactions", "power", "punishment").setValue(1.0).setComment("Punishment after killing a teammate.");
        get().getNode("eaglefactions", "friendlyfire", "alliance").setValue(false).setComment("Allows/denies friendly fire between alliances.");
        get().getNode("eaglefactions", "spawn", "mobs").setValue(false).setComment("Allows/denies mob spawning on factions lands.");
    }

    @Override
    public CommentedConfigurationNode get()
    {
        return configNode;
    }
}
