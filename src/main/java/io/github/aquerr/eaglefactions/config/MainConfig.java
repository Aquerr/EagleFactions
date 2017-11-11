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
        get().getNode("eaglefactions", "name", "maxlength").setValue(30).setComment("This determines the maximum amount of characters a Factions's name can be. Default: 30");
        get().getNode("eaglefactions", "name", "minlength").setValue(3).setComment("This determines the minimum amount of characters a Factions's name can be. Default: 3");
        get().getNode("eaglefactions", "tag", "maxlength").setValue(5).setComment("This determines the minimum amount of characters a Factions's tag can be. Default: 5");
        get().getNode("eaglefactions", "tag", "minlength").setValue(2).setComment("This determines the minimum amount of characters a Factions's tag can be. Default: 2");
        get().getNode("eaglefactions", "prefix", "display").setValue(true).setComment("Allows/denies displaying Faction prefixes. Default: true");
        get().getNode("eaglefactions", "power", "increment").setValue(0.04).setComment("How much power will be restored for player after 1 minute of playing. (0.04 per minute = 1,2 per hour.) Default: 0.04");
        get().getNode("eaglefactions", "power", "decrement").setValue(2.0).setComment("How much power will be removed on player death. Default: 2.0");
        get().getNode("eaglefactions", "power", "maxpower").setValue(10.0).setComment("Maximum amount of power a player can have. Default: 10.0");
        get().getNode("eaglefactions", "power", "startpower").setValue(5.0).setComment("Starting amount of power. Default: 5.0");
        get().getNode("eaglefactions", "power", "killaward").setValue(2.0).setComment("Player kill award. Default: 2.0");
        get().getNode("eaglefactions", "power", "punishment").setValue(1.0).setComment("Punishment after killing a teammate. Default: 1.0");
        get().getNode("eaglefactions", "friendlyfire", "alliance").setValue(false).setComment("Allows/denies friendly fire between alliances. Default: false");
        get().getNode("eaglefactions", "spawn", "mobs").setValue(false).setComment("Allows/denies mob spawning on factions lands. Default: false");
        get().getNode("eaglefactions", "gameplay", "blockEnteringFactions").setValue(true).setComment("Blocks entering faction's lands that players are offline. Default: true");
        get().getNode("eaglefactions", "gameplay", "connectedClaims").setValue(true).setComment("Require claims to be connected? Default: true");
        get().getNode("eaglefactions", "gameplay", "blockSafeZoneWhileInWarZone").setValue(false).setComment("Block entering to the SafeZone from the WarZone. Default: false");
        get().getNode("eaglefactions", "playerlimit", "playerlimit").setValue(false).setComment("Turns on/off player limit in factions. Default: false");
        get().getNode("eaglefactions", "playerlimit", "limit").setValue(15).setComment("Player limit in the faction. Default: 15");
    }

    @Override
    public CommentedConfigurationNode get()
    {
        return configNode;
    }
}
