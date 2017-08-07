package io.github.aquerr.eaglefactions.services;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PowerService
{
    public static Timer timerPower = new Timer();
    public static TimerTask increasePower = new TimerTask()
    {
        @Override
        public void run()
        {

        }
    };

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

            playerNode.getNode("power").setValue(MainLogic.getStartingPower());
            playerNode.getNode("maxpower").setValue(MainLogic.getGlobalMaxPower());
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static double getPlayerPower(UUID playerUUID)
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
                     double playerPower = playerNode.getNode("power").getDouble();
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

    public static double getFactionPower(Faction faction)
    {
        double factionPower = 0;

        if(faction.Leader != null)
        {
            factionPower += getPlayerPower(faction.Leader);
        }

        if(faction.Officers != null && !faction.Officers.isEmpty())
        {
            for (String officer: faction.Officers)
            {
                double officerPower = getPlayerPower(UUID.fromString(officer));
                factionPower += officerPower;
            }
        }

        if(faction.Members != null && !faction.Members.isEmpty())
        {
            for (String member: faction.Members)
            {
                double memberPower = getPlayerPower(UUID.fromString(member));
                factionPower += memberPower;
            }
        }

        return factionPower;
    }

    public static double getFactionMaxPower(Faction faction)
    {
        double factionMaxPower = 0;

        if(faction.Leader != null)
        {
            factionMaxPower += PowerService.getPlayerMaxPower(faction.Leader);
        }

        if(faction.Officers != null && !faction.Officers.isEmpty())
        {
            for (String officer: faction.Officers)
            {
                factionMaxPower += PowerService.getPlayerMaxPower(UUID.fromString(officer));
            }
        }

        if(faction.Members != null && !faction.Members.isEmpty())
        {
            for (String member: faction.Members)
            {
                factionMaxPower += PowerService.getPlayerMaxPower(UUID.fromString(member));
            }
        }

        return factionMaxPower;
    }

    public static double getPlayerMaxPower(UUID playerUUID)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            double playerMaxPower = playerNode.getNode("maxpower").getDouble();

            return playerMaxPower;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return 0;
    }

    public static void addPower(UUID playerUUID)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            double playerPower = playerNode.getNode("power").getDouble();

            playerNode.getNode("power").setValue(playerPower + MainLogic.getPowerIncrement());
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    public static void setPower(UUID playerUUID, double power)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("power").setValue(power);
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static void increasePower(UUID playerUUID)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.execute(new Runnable()
        {
            @Override
            public void run()
            {
                if(!PlayerService.isPlayerOnline(playerUUID)) return;

                if(PowerService.getPlayerPower(playerUUID) + MainLogic.getPowerIncrement() < PowerService.getPlayerMaxPower(playerUUID))
                {
                    PowerService.addPower(playerUUID);
                    increasePower(playerUUID);
                }
                else
                {
                    PowerService.setPower(playerUUID, PowerService.getPlayerMaxPower(playerUUID));
                    increasePower(playerUUID);
                }
            }
        }).delay(30, TimeUnit.MINUTES).name("Eaglefactions - Increase power scheduler").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());
    }
}
