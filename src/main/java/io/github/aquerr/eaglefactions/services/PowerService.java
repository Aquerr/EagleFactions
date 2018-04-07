package io.github.aquerr.eaglefactions.services;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PowerService
{
    //private static IConfig factionsConfig = FactionsConfig.getConfig();

    private static CommentedConfigurationNode _factionsNode;
    private static Path playersPath;

    public static void setup(Path configDir)
    {
        try
        {
            _factionsNode = HoconConfigurationLoader.builder().setPath(Paths.get(configDir.resolve("data") + "/factions.conf")).build().load();
            playersPath = configDir.resolve("players");
            if (!Files.exists(playersPath)) Files.createDirectory(playersPath);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static boolean checkIfPlayerExists(UUID playerUUID)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");
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
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

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
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        if(checkIfPlayerExists(playerUUID))
        {
            try
            {
                ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

                CommentedConfigurationNode playerNode = configLoader.load();

                 if(playerNode.getNode("power").getValue() != null)
                 {
                     BigDecimal playerPower =  new BigDecimal(playerNode.getNode("power").getString());
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
        if(faction.Name.equals("SafeZone") || faction.Name.equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.Name, "power");

            BigDecimal factionPowerInFile = new BigDecimal(powerNode.getDouble());

            return factionPowerInFile;
        }

        BigDecimal factionPower = BigDecimal.ZERO;
        if(faction.Leader != null && !faction.Leader.equals(""))
        {
            factionPower = factionPower.add(getPlayerPower(UUID.fromString(faction.Leader)));
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

        return factionPower;
    }

    public static BigDecimal getFactionMaxPower(Faction faction)
    {
        if(faction.Name.equals("SafeZone") || faction.Name.equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.Name, "power");

            BigDecimal factionPowerInFile = new BigDecimal(powerNode.getDouble());

            return factionPowerInFile;
        }

        BigDecimal factionMaxPower = BigDecimal.ZERO;

        if(faction.Leader != null && !faction.Leader.equals(""))
        {
            factionMaxPower = factionMaxPower.add(PowerService.getPlayerMaxPower(UUID.fromString(faction.Leader)));
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
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

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
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

            if(PowerService.getPlayerPower(playerUUID).add(MainLogic.getPowerIncrement()).doubleValue() < PowerService.getPlayerMaxPower(playerUUID).doubleValue())
            {
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
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    public static void setPower(UUID playerUUID, BigDecimal power)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

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

        taskBuilder.interval(1, TimeUnit.MINUTES).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {
                if (!PlayerService.isPlayerOnline(playerUUID)) task.cancel();

                if(PowerService.getPlayerPower(playerUUID).add(MainLogic.getPowerIncrement()).doubleValue() < PowerService.getPlayerMaxPower(playerUUID).doubleValue())
                {
                    PowerService.addPower(playerUUID, false);
                }
                else
                {
                    PowerService.setPower(playerUUID, PowerService.getPlayerMaxPower(playerUUID));
                }
            }
        }).submit(EagleFactions.getEagleFactions());
    }

    public static void decreasePower(UUID playerUUID)
    {
        if(PowerService.getPlayerPower(playerUUID).subtract(MainLogic.getPowerDecrement()).doubleValue() > BigDecimal.ZERO.doubleValue())
        {
            Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

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

    public static void penalty(UUID playerUUID)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

            BigDecimal penalty = MainLogic.getPenalty();

            if(playerPower.doubleValue() - penalty.doubleValue() > 0)
            {
                playerNode.getNode("power").setValue(playerPower.subtract(penalty));
            }
            else
            {
                playerNode.getNode("power").setValue(0.0);
            }

            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static void setMaxPower(UUID playerUUID, BigDecimal power)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("maxpower").setValue(power);
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
