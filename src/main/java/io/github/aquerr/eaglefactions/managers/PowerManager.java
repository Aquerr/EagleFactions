package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.IConfiguration;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
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
    private ConfigFields _configFields;
    private CommentedConfigurationNode _factionsNode;
    private Path playersPath;

    public PowerManager(IConfiguration configuration, Path configDir)
    {
        _configFields = configuration.getConfigFileds();

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

    public boolean checkIfPlayerExists(UUID playerUUID)
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

    public void addPlayer(UUID playerUUID)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            Files.createFile(playerFile);

            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("power").setValue(_configFields.getStartingPower());
            playerNode.getNode("maxpower").setValue(_configFields.getGlobalMaxPower());
            configLoader.save(playerNode);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public BigDecimal getPlayerPower(UUID playerUUID)
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

    public BigDecimal getFactionPower(Faction faction)
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

    public BigDecimal getFactionMaxPower(Faction faction)
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
            factionMaxPower = factionMaxPower.add(getPlayerMaxPower(faction.getLeader()));
        }

        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer : faction.getOfficers())
            {
                factionMaxPower = factionMaxPower.add(getPlayerMaxPower(officer));
            }
        }

        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member : faction.getMembers())
            {
                factionMaxPower = factionMaxPower.add(getPlayerMaxPower(member));
            }
        }

        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                factionMaxPower = factionMaxPower.add(getPlayerMaxPower(recruit));
            }
        }

        return factionMaxPower;
    }

    public BigDecimal getPlayerMaxPower(UUID playerUUID)
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
                playerNode.getNode("maxpower").setValue(_configFields.getGlobalMaxPower());

                return _configFields.getGlobalMaxPower();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public void addPower(UUID playerUUID, boolean isKillAward)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

            if(getPlayerPower(playerUUID).add(_configFields.getPowerIncrement()).doubleValue() < getPlayerMaxPower(playerUUID).doubleValue())
            {
                if(isKillAward)
                {
                    BigDecimal killAward = _configFields.getKillAward();
                    playerNode.getNode("power").setValue(playerPower.add(killAward));
                }
                else
                {
                    playerNode.getNode("power").setValue(playerPower.add(_configFields.getPowerIncrement()));
                }

                configLoader.save(playerNode);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    public void setPower(UUID playerUUID, BigDecimal power)
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

    public void startIncreasingPower(UUID playerUUID)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.MINUTES).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {
                if (!PlayerManager.isPlayerOnline(playerUUID)) task.cancel();

                if(getPlayerPower(playerUUID).add(_configFields.getPowerIncrement()).doubleValue() < getPlayerMaxPower(playerUUID).doubleValue())
                {
                    addPower(playerUUID, false);
                }
                else
                {
                    setPower(playerUUID, getPlayerMaxPower(playerUUID));
                }
            }
        }).submit(EagleFactions.getPlugin());
    }

    public void decreasePower(UUID playerUUID)
    {
        if(getPlayerPower(playerUUID).subtract(_configFields.getPowerDecrement()).doubleValue() > BigDecimal.ZERO.doubleValue())
        {
            Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

            try
            {
                ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

                CommentedConfigurationNode playerNode = configLoader.load();

                BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

                playerNode.getNode("power").setValue(playerPower.subtract(_configFields.getPowerDecrement()));
                configLoader.save(playerNode);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
        else
        {
            setPower(playerUUID, BigDecimal.ZERO);
        }
    }

    public void penalty(UUID playerUUID)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            BigDecimal playerPower = new BigDecimal(playerNode.getNode("power").getString());

            BigDecimal penalty = _configFields.getPenalty();

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

    public void setMaxPower(UUID playerUUID, BigDecimal power)
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
