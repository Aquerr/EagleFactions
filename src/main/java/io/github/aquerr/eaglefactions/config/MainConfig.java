package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.EagleFactions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class MainConfig implements IConfig
{
    private static MainConfig mainConfig = new MainConfig();

    private MainConfig()
    {

    }

    public static MainConfig getConfig()
    {
        return mainConfig;
    }

    private Path configPath = Paths.get(EagleFactions.getEagleFactions ().getConfigDir() + "/Settings.conf");
    private ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configPath).build();
    private CommentedConfigurationNode configNode;

    @Override
    public void setup()
    {
        if (!Files.exists(configPath))
        {
            try
            {
                Files.createDirectory(EagleFactions.getEagleFactions().getConfigDir());

                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Settings.conf");
                Files.copy(inputStream, configPath);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
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
//        get().getNode("eaglefactions").setComment("Contains all Eagle Factions related settings.");
//        get().getNode("eaglefactions", "name", "maxlength").setValue(30).setComment("This determines the maximum amount of characters a Factions's name can be. Default: 30");
//        get().getNode("eaglefactions", "name", "minlength").setValue(3).setComment("This determines the minimum amount of characters a Factions's name can be. Default: 3");
//        get().getNode("eaglefactions", "tag", "maxlength").setValue(5).setComment("This determines the minimum amount of characters a Factions's tag can be. Default: 5");
//        get().getNode("eaglefactions", "tag", "minlength").setValue(2).setComment("This determines the minimum amount of characters a Factions's tag can be. Default: 2");
//        //get().getNode("eaglefactions", "prefix", "display").setValue(true).setComment("Allows/denies displaying Faction prefixes. Default: true");
//        get().getNode("eaglefactions", "chat", "factionprefix").setValue("tag").setComment("Should faction name or tag be displayed in chat when someone is writing? Default: tag (Available Options: tag, name, none)");
//        get().getNode("eaglefactions", "chat", "factionrank").setValue(true).setComment("Should faction rank (leader, officer) be displayed in chat when someone is writing? Default: true");
//        get().getNode("eaglefactions", "power", "increment").setValue(0.04).setComment("How much power will be restored for player after 1 minute of playing. (0.04 per minute = 1,2 per hour.) Default: 0.04");
//        get().getNode("eaglefactions", "power", "decrement").setValue(2.0).setComment("How much power will be removed on player death. Default: 2.0");
//        get().getNode("eaglefactions", "power", "maxpower").setValue(10.0).setComment("Maximum amount of power a player can have. Default: 10.0");
//        get().getNode("eaglefactions", "power", "startpower").setValue(5.0).setComment("Starting amount of power. Default: 5.0");
//        get().getNode("eaglefactions", "power", "killaward").setValue(2.0).setComment("Player kill award. Default: 2.0");
//        get().getNode("eaglefactions", "power", "punishment").setValue(1.0).setComment("Punishment after killing a teammate. Default: 1.0");
//        get().getNode("eaglefactions", "friendlyfire", "alliance").setValue(false).setComment("Allows/denies friendly fire between alliances. Default: false");
//        get().getNode("eaglefactions", "spawn", "mobs").setValue(false).setComment("Allows/denies mob spawning on factions lands. Default: false");
//        get().getNode("eaglefactions", "spawn", "spawnAtHomeAfterDeath").setValue(false).setComment("Should player spawn at faction's home after death? Default: false");
//        get().getNode("eaglefactions", "claims", "Delayed_Claim").setValue(false).setComment("Should it take some time to claim a territory? Default: false");
//        get().getNode("eaglefactions", "claims", "Claiming_Time").setValue(10).setComment("How much time in seconds should claiming take? (Delayed_Claim must be set to true for this to work) Default: 10");
//        get().getNode("eaglefactions", "claims", "Claiming_By_Items").setComment("Here you can find all options for claiming by using items.");
//        get().getNode("eaglefactions", "claims", "Claiming_By_Items", "Turned_On").setValue(false).setComment("Allows/denies using items/blocks to claim a territory. Default: false");
//        get().getNode("eaglefactions", "claims", "Claiming_By_Items", "Items").setValue(new ArrayList<>(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20", "minecraft:iron_ingot|4"))).setComment("A list of items/blocks that will be taken from the player after creating a faction." + "\n" +
//                "There is a simple list of items below which you can edit by yourself. Current list contains: 35 orange wool, 20 wooden planks, 4 iron ingots." + "\n" +
//                "Write every item/block in format 35:1|42 where 35:1 is an item/block id and 42 is an amount.");
//        get().getNode("eaglefactions", "gameplay", "blockEnteringFactions").setValue(true).setComment("Blocks entering faction's lands that players are offline. Default: true");
//        get().getNode("eaglefactions", "gameplay", "connectedClaims").setValue(true).setComment("Require claims to be connected? Default: true");
//        get().getNode("eaglefactions", "gameplay", "blockSafeZoneWhileInWarZone").setValue(false).setComment("Block entering to the SafeZone from the WarZone. Default: false");
//        get().getNode("eaglefactions", "gameplay", "attacktime").setValue(10).setComment("How much time in seconds takes destroying a claim. Default: 10");
//        get().getNode("eaglefactions", "gameplay", "attackOnlyAtNight").setValue(false).setComment("Should attacking other factions (using /f attack command) be able only at night? Default: false");
//        get().getNode("eaglefactions", "gameplay", "factioncreation").setComment("Faction creation node. You can choose if faction should be created by items or for free.");
//        get().getNode("eaglefactions", "gameplay", "factioncreation", "createbyitems").setValue(false).setComment("Allows/denies using items/blocks to create a faction. Default: false");
//        get().getNode("eaglefactions", "gameplay", "factioncreation", "items").setValue(new ArrayList<>(Arrays.asList("minecraft:wool:1|35", "minecraft:planks|20", "minecraft:iron_ingot|4"))).setComment("A list of items/blocks that will be taken from the player after creating a faction." + "\n" +
//                "There is a simple list of items below which you can edit by yourself. Current list contains: 35 orange wool, 20 wooden planks, 4 iron ingots." + "\n" +
//                "Write every item/block in format 35:1|42 where 35:1 is an item/block id and 42 is an amount.");
//        //get().getNode("eaglefactions", "gameplay", "factioncreation", "createbyplacingblock")
//        get().getNode("eaglefactions", "playerlimit", "playerlimit").setValue(false).setComment("Turns on/off player limit in factions. Default: false");
//        get().getNode("eaglefactions", "playerlimit", "limit").setValue(15).setComment("Player limit in the faction. Default: 15");
//        get().getNode("eaglefactions", "home", "teleportBetweenWorlds").setValue(false).setComment("Should players be able to teleport to faction's home while being in other world? Default: false");
//        get().getNode("eaglefactions", "home", "Block_Home_After_Death_In_Own_Faction", "Turned_On").setValue(false).setComment("Block using of /home command after being killed in own faction territory? Default: false");
//        get().getNode("eaglefactions", "home", "Block_Home_After_Death_In_Own_Faction", "Time").setValue(60).setComment("How much time in seconds should player be blocked before using Home Command? Default: 60");
    }

    @Override
    public CommentedConfigurationNode get()
    {
        return configNode;
    }
}
