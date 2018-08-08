package io.github.aquerr.eaglefactions.managers;

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

public class PowerManager
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
        if(faction.getName().equals("SafeZone") || faction.getName().equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.getName(), "power");

            BigDecimal factionPowerInFile = new BigDecimal(powerNode.getDouble());

            return factionPowerInFile;
        }

        BigDecimal factionPower = BigDecimal.ZERO;
        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            factionPower = factionPower.add(getPlayerPower(faction.getLeader()));
        }
        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer: faction.getOfficers())
            {
                BigDecimal officerPower = getPlayerPower(officer);
                factionPower =factionPower.add(officerPower);
            }
        }
        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member: faction.getMembers())
            {
                BigDecimal memberPower = getPlayerPower(member);
                factionPower = factionPower.add(memberPower);
            }
        }
        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                BigDecimal recruitPower = getPlayerPower(recruit);
                factionPower = factionPower.add(recruitPower);
            }
        }

        return factionPower;
    }

    public static BigDecimal getFactionMaxPower(Faction faction)
    {
        if(faction.getName().equals("SafeZone") || faction.getName().equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.getName(), "power");

            BigDecimal factionPowerInFile = new BigDecimal(powerNode.getDouble());

            return factionPowerInFile;
        }

        BigDecimal factionMaxPower = BigDecimal.ZERO;

        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            factionMaxPower = factionMaxPower.add(PowerManager.getPlayerMaxPower(faction.getLeader()));
        }

        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer : faction.getOfficers())
            {
                factionMaxPower = factionMaxPower.add(PowerManager.getPlayerMaxPower(officer));
            }
        }

        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member : faction.getMembers())
            {
                factionMaxPower = factionMaxPower.add(PowerManager.getPlayerMaxPower(member));
            }
        }

        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                factionMaxPower = factionMaxPower.add(PowerManager.getPlayerMaxPower(recruit));
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

            Object value = playerNode.getNode("maxpower").getValue();

            if (value != null)
            {
                BigDecimal playerMaxPower =  new BigDecimal(value.toString());

                return playerMaxPower;
            }
            else
            {
                playerNode.getNode("maxpower").setValue(MainLogic.getGlobalMaxPower());

                return MainLogic.getGlobalMaxPower();
            }
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

            if(PowerManager.getPlayerPower(playerUUID).add(MainLogic.getPowerIncrement()).doubleValue() < PowerManager.getPlayerMaxPower(playerUUID).doubleValue())
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

    public static void startIncreasingPower(UUID playerUUID)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.MINUTES).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {
                if (!PlayerManager.isPlayerOnline(playerUUID)) task.cancel();

                if(PowerManager.getPlayerPower(playerUUID).add(MainLogic.getPowerIncrement()).doubleValue() < PowerManager.getPlayerMaxPower(playerUUID).doubleValue())
                {
                    PowerManager.addPower(playerUUID, false);
                }
                else
                {
                    PowerManager.setPower(playerUUID, PowerManager.getPlayerMaxPower(playerUUID));
                }
            }
        }).submit(EagleFactions.getPlugin());
    }

    public static void decreasePower(UUID playerUUID)
    {
        if(PowerManager.getPlayerPower(playerUUID).subtract(MainLogic.getPowerDecrement()).doubleValue() > BigDecimal.ZERO.doubleValue())
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
            PowerManager.setPower(playerUUID, BigDecimal.ZERO);
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
//                updateFactionPower(playerUUID, penalty, false);
            }
            else
            {
                playerNode.getNode("power").setValue(0.0);
//                updateFactionPower(playerUUID, penalty, false);
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

//    public static void updateFactionPower(UUID playerUUID, BigDecimal power, boolean increment)
//    {
//        Optional<Faction> optionalFaction = FactionLogic.getFactionByPlayerUUID(playerUUID);
//
//        if (optionalFaction.isPresent())
//        {
//
//        }
//    }
}
