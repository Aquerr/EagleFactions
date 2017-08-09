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
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

            playerNode.getNode("power").setValue(MainLogic.getStartingPower());
            playerNode.getNode("maxpower").setValue(MainLogic.getGlobalMaxPower());
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static BigDecimal getPlayerPower(UUID playerUUID)
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
                     BigDecimal playerPower =  new BigDecimal(playerNode.getNode("power").getString());
                     EagleFactions.getEagleFactions().getLogger().info("Successfully got playerPower = " + playerPower.toString());
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
        return BigDecimal.ZERO;
    }

    public static BigDecimal getFactionPower(Faction faction)
    {
        BigDecimal factionPower = BigDecimal.ZERO;

        if(faction.Leader != null)
        {
            factionPower = factionPower.add(getPlayerPower(faction.Leader));
            EagleFactions.getEagleFactions().getLogger().info("Leader power: " + getPlayerPower(faction.Leader).toString());
            EagleFactions.getEagleFactions().getLogger().info("Faction power: " + factionPower.toString());
        }

        if(faction.Officers != null && !faction.Officers.isEmpty())
        {
            for (String officer: faction.Officers)
            {
                BigDecimal officerPower = getPlayerPower(UUID.fromString(officer));
                factionPower =factionPower.add(officerPower);
            }
        }

        if(faction.Members != null && !faction.Members.isEmpty())
        {
            for (String member: faction.Members)
            {
                BigDecimal memberPower = getPlayerPower(UUID.fromString(member));
                factionPower = factionPower.add(memberPower);
            }
        }
        EagleFactions.getEagleFactions().getLogger().info(factionPower.toString());
        return factionPower;
    }

    public static BigDecimal getFactionMaxPower(Faction faction)
    {
        BigDecimal factionMaxPower = BigDecimal.ZERO;

        if(faction.Leader != null)
        {
            factionMaxPower = factionMaxPower.add(PowerService.getPlayerMaxPower(faction.Leader));
        }

        if(faction.Officers != null && !faction.Officers.isEmpty())
        {
            for (String officer: faction.Officers)
            {
                factionMaxPower = factionMaxPower.add(PowerService.getPlayerMaxPower(UUID.fromString(officer)));
            }
        }

        if(faction.Members != null && !faction.Members.isEmpty())
        {
            for (String member: faction.Members)
            {
                factionMaxPower = factionMaxPower.add(PowerService.getPlayerMaxPower(UUID.fromString(member)));
            }
        }

        return factionMaxPower;
    }

    public static BigDecimal getPlayerMaxPower(UUID playerUUID)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            BigDecimal playerMaxPower =  new BigDecimal(playerNode.getNode("maxpower").getString());

            return playerMaxPower;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public static void addPower(UUID playerUUID, boolean isKillAward)
    {
        Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

            if(isKillAward)
            {
                BigDecimal killAward = MainLogic.getKillAward();
                playerNode.getNode("power").setValue(playerPower.add(killAward));
            }
            else
            {
                playerNode.getNode("power").setValue(playerPower.add(MainLogic.getPowerIncrement()));
            }
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    public static void setPower(UUID playerUUID, BigDecimal power)
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

                if(PowerService.getPlayerPower(playerUUID).add(MainLogic.getPowerIncrement()).doubleValue() < PowerService.getPlayerMaxPower(playerUUID).doubleValue())
                {
                    PowerService.addPower(playerUUID, false);
                    increasePower(playerUUID);
                }
                else
                {
                    PowerService.setPower(playerUUID, PowerService.getPlayerMaxPower(playerUUID));
                    increasePower(playerUUID);
                }
            }
        }).delay(1, TimeUnit.MINUTES).name("Eaglefactions - Increase power scheduler").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());
    }

    public static void decreasePower(UUID playerUUID)
    {
        if(PowerService.getPlayerPower(playerUUID).subtract(MainLogic.getPowerDecrement()).doubleValue() > BigDecimal.ZERO.doubleValue())
        {
            Path playerFile = Paths.get(EagleFactions.getEagleFactions ().getConfigDir().resolve("players") +  "/" + playerUUID.toString() + ".conf");

            try
            {
                ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

                CommentedConfigurationNode playerNode = configLoader.load();

                BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

                playerNode.getNode("power").setValue(playerPower.subtract(MainLogic.getPowerDecrement()));
                configLoader.save(playerNode);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        else
        {
            PowerService.setPower(playerUUID, BigDecimal.ZERO);
        }
    }
}
