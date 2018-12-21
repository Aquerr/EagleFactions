package io.github.aquerr.eaglefactions.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.entities.Faction;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Singleton
public class PowerManager
{
    private static PowerManager instance = null;

    private final EagleFactions _plugin;
    private final ConfigFields _configFields;

    private CommentedConfigurationNode _factionsNode;
    private UUID dummyUUID = new UUID(0, 0);

    public static PowerManager getInstance(EagleFactions eagleFactions)
    {
        if (instance == null)
            return new PowerManager(eagleFactions);
        else return instance;
    }

    private PowerManager(EagleFactions eagleFactions)
    {
        instance = this;
        _plugin = eagleFactions;
        _configFields = eagleFactions.getConfiguration().getConfigFields();
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

    public float getPlayerPower(@Nullable UUID playerUUID)
    {
        if (playerUUID == null || playerUUID.equals(dummyUUID))
            return 0;
        return _plugin.getPlayerManager().getPlayerPower(playerUUID);
    }

    public float getFactionPower(Faction faction)
    {
        if(faction.getName().equals("SafeZone") || faction.getName().equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.getName(), "power");

            return powerNode.getFloat(9999f);
        }

        float factionPower = 0;
        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            factionPower = factionPower + getPlayerPower(faction.getLeader());
        }
        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer: faction.getOfficers())
            {
                float officerPower = getPlayerPower(officer);
                factionPower =factionPower + officerPower;
            }
        }
        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member: faction.getMembers())
            {
                float memberPower = getPlayerPower(member);
                factionPower = factionPower + memberPower;
            }
        }
        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                float recruitPower = getPlayerPower(recruit);
                factionPower = factionPower + recruitPower;
            }
        }

        return factionPower;
    }

    public float getFactionMaxPower(Faction faction)
    {
        if(faction.getName().equals("SafeZone") || faction.getName().equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.getName(), "power");

            return powerNode.getFloat(9999f);
        }

        float factionMaxPower = 0;

        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            factionMaxPower = factionMaxPower + getPlayerMaxPower(faction.getLeader());
        }

        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer : faction.getOfficers())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(officer);
            }
        }

        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member : faction.getMembers())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(member);
            }
        }

        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(recruit);
            }
        }

        return factionMaxPower;
    }

    public float getPlayerMaxPower(UUID playerUUID)
    {
        if(playerUUID == null || playerUUID.equals(dummyUUID))
            return 0;

        return _plugin.getPlayerManager().getPlayerMaxPower(playerUUID);
    }

    public void addPower(UUID playerUUID, boolean isKillAward)
    {
        float playerPower = _plugin.getPlayerManager().getPlayerPower(playerUUID);

        if(playerPower + _configFields.getPowerIncrement() < getPlayerMaxPower(playerUUID))
        {
            if(isKillAward)
            {
                float killAward = _configFields.getKillAward();
                _plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower + killAward);
            }
            else
            {
                _plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower + _configFields.getPowerIncrement());
            }
        }
    }

    public void setPower(UUID playerUUID, float power)
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

                if(getPlayerPower(playerUUID) + _configFields.getPowerIncrement() < getPlayerMaxPower(playerUUID))
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
        float playerPower = _plugin.getPlayerManager().getPlayerPower(playerUUID);

        if(playerPower - _configFields.getPowerDecrement() > 0)
        {
                _plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower - _configFields.getPowerDecrement());
        }
        else
        {
            setPower(playerUUID, 0);
        }
    }

    public void penalty(UUID playerUUID)
    {
        float playerPower = _plugin.getPlayerManager().getPlayerPower(playerUUID);
        float penalty = _configFields.getPenalty();

        if(playerPower - penalty > 0)
        {
            _plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower - penalty);
        }
        else
        {
            _plugin.getPlayerManager().setPlayerPower(playerUUID, 0);
        }
    }

    public void setMaxPower(UUID playerUUID, float power)
    {
        _plugin.getPlayerManager().setPlayerMaxPower(playerUUID, power);
    }
}
