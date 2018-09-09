package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
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
    private EagleFactions _plugin;

    private ConfigFields _configFields;
    private CommentedConfigurationNode _factionsNode;

    private UUID dummyUUID = new UUID(0, 0);

    public PowerManager(EagleFactions eagleFactions)
    {
        _plugin = eagleFactions;
        _configFields = eagleFactions.getConfiguration().getConfigFileds();
        Path configDir = eagleFactions.getConfigDir();

        try
        {
            _factionsNode = HoconConfigurationLoader.builder().setPath(Paths.get(configDir.resolve("data") + "/factions.conf")).build().load();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public BigDecimal getPlayerPower(@Nullable UUID playerUUID)
    {
        if (playerUUID == null || playerUUID.equals(dummyUUID))
            return BigDecimal.ZERO;
        return _plugin.getPlayerManager().getPlayerPower(playerUUID);
    }

    public BigDecimal getFactionPower(Faction faction)
    {
        if(faction.getName().equals("SafeZone") || faction.getName().equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.getName(), "power");

            return new BigDecimal(powerNode.getDouble());
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

            return new BigDecimal(powerNode.getDouble());
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
        if(playerUUID == null || playerUUID.equals(dummyUUID))
            return BigDecimal.ZERO;

        return _plugin.getPlayerManager().getPlayerMaxPower(playerUUID);
    }

    public void addPower(UUID playerUUID, boolean isKillAward)
    {
        BigDecimal playerPower = _plugin.getPlayerManager().getPlayerPower(playerUUID);

        if(playerPower.add(_configFields.getPowerIncrement()).doubleValue() < getPlayerMaxPower(playerUUID).doubleValue())
        {
            if(isKillAward)
            {
                BigDecimal killAward = _configFields.getKillAward();
                _plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower.add(killAward));
            }
            else
            {
                _plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower.add(_configFields.getPowerIncrement()));
            }
        }
    }

    public void setPower(UUID playerUUID, BigDecimal power)
    {
        _plugin.getPlayerManager().setPlayerPower(playerUUID, power);
    }

    public void startIncreasingPower(UUID playerUUID)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.MINUTES).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {
                if (!_plugin.getPlayerManager().isPlayerOnline(playerUUID)) task.cancel();

                if(getPlayerPower(playerUUID).add(_configFields.getPowerIncrement()).doubleValue() < getPlayerMaxPower(playerUUID).doubleValue())
                {
                    addPower(playerUUID, false);
                }
                else
                {
                    setPower(playerUUID, getPlayerMaxPower(playerUUID));
                }
            }
        }).async().submit(_plugin);
    }

    public void decreasePower(UUID playerUUID)
    {
        BigDecimal playerPower = _plugin.getPlayerManager().getPlayerPower(playerUUID);

        if(playerPower.subtract(_configFields.getPowerDecrement()).doubleValue() > BigDecimal.ZERO.doubleValue())
        {
                _plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower.subtract(_configFields.getPowerDecrement()));
        }
        else
        {
            setPower(playerUUID, BigDecimal.ZERO);
        }
    }

    public void penalty(UUID playerUUID)
    {
        BigDecimal playerPower = _plugin.getPlayerManager().getPlayerPower(playerUUID);
        BigDecimal penalty = _configFields.getPenalty();

        if(playerPower.doubleValue() - penalty.doubleValue() > 0)
        {
            _plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower.subtract(penalty));
        }
        else
        {
            _plugin.getPlayerManager().setPlayerPower(playerUUID, new BigDecimal(0.0));
        }
    }

    public void setMaxPower(UUID playerUUID, BigDecimal power)
    {
        _plugin.getPlayerManager().setPlayerMaxPower(playerUUID, power);
    }
}
