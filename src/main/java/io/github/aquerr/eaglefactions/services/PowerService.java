package io.github.aquerr.eaglefactions.services;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.entity.living.player.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class PowerService
{
    public static boolean checkIfPlayerExists(UUID playerUUID)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");
        if(Files.exists(playerFile))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static void addPlayer(UUID playerUUID)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        try
        {
            Files.createFile(playerFile);

            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("power").setValue(10);
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static int getPlayerPower(UUID playerUUID)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        if(checkIfPlayerExists(playerUUID))
        {
            try
            {
                ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

                CommentedConfigurationNode playerNode = configLoader.load();

                 if(playerNode.getNode("power").getValue() != null)
                 {
                     int playerPower = playerNode.getInt();
                     return playerPower;
                 }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        else
        {
            addPlayer(playerUUID);
            return getPlayerPower(playerUUID);
        }
        return 0;
    }

    public static int getFactionPower(Faction faction)
    {
        int factionPower = 0;

        if(faction.Leader != null)
        {
            factionPower += getPlayerPower(faction.Leader);
        }

        if(faction.Officers != null && !faction.Officers.isEmpty())
        {
            for (String officer: faction.Officers)
            {
                int officerPower = getPlayerPower(UUID.fromString(officer));
                factionPower += officerPower;
            }
        }

        if(faction.Members != null && !faction.Members.isEmpty())
        {
            for (String member: faction.Members)
            {
                int memberPower = getPlayerPower(UUID.fromString(member));
                factionPower += memberPower;
            }
        }

        return factionPower;
    }
}
