package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.config.IConfiguration;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PVPLogger
{
    private ConfigFields _configFields;
    private Map<UUID, Integer> _attackedPlayers;
    private boolean _isActive;
    private int _blockTime;
    private List<String> _blockedCommandsDuringFight;

    public PVPLogger(IConfiguration configuration)
    {
        _configFields = configuration.getConfigFileds();
        _isActive = _configFields.isPVPLoggerActive();

        if (_isActive)
        {
            _attackedPlayers = new HashMap<>();
            _blockTime = _configFields.getPVPLoggerBlockTime();
            _blockedCommandsDuringFight = _configFields.getBlockedCommandsDuringFight();
        }
    }

    public boolean isActive()
    {
        return _isActive;
    }

    public int getBlockTime()
    {
        return _blockTime;
    }

    public boolean shouldBlockCommand(Player player, String usedCommand)
    {
        if (isPlayerBlocked(player))
        {
            if (usedCommand.charAt(0) == '/')
            {
                usedCommand = usedCommand.substring(1);
            }

            usedCommand = usedCommand.toLowerCase();

            for (String blockedCommand : _blockedCommandsDuringFight)
            {
                if (blockedCommand.charAt(0) == '/')
                {
                    blockedCommand = blockedCommand.substring(1);
                }

                if (blockedCommand.equals("*") || usedCommand.equals(blockedCommand) || usedCommand.startsWith(blockedCommand))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public void addOrUpdatePlayer(Player player)
    {
        //Update player's time if it already in a list.

        synchronized(_attackedPlayers)
        {
            if (_attackedPlayers.containsKey(player.getUniqueId()))
            {
                _attackedPlayers.replace(player.getUniqueId(), getBlockTime());
            }
            else
            {
                _attackedPlayers.put(player.getUniqueId(), getBlockTime());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, PluginMessages.PVPLOGGER_HAS_TURNED_ON + " " + PluginMessages.YOU_WILL_DIE_IF_YOU_DISCONNECT_IN + " " + getBlockTime() + " " + PluginMessages.SECONDS + "!"));

                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
                taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
                {
                    @Override
                    public void accept(Task task)
                    {
                        if (_attackedPlayers.containsKey(player.getUniqueId()))
                        {
                            int seconds = _attackedPlayers.get(player.getUniqueId());

                            if (seconds <= 0)
                            {
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.PVPLOGGER_HAS_TURNED_OFF + " " + PluginMessages.YOU_CAN_NOW_DISCONNECT_SAFELY));
                                _attackedPlayers.remove(player.getUniqueId());
                                task.cancel();
                            }
                            else
                            {
                                _attackedPlayers.replace(player.getUniqueId(), seconds, seconds - 1);
                            }
                        }
                        else
                        {
                            task.cancel();
                        }
                    }
                }).async().submit(EagleFactions.getPlugin());
            }
        }
    }

    public boolean isPlayerBlocked(Player player)
    {
        if (_attackedPlayers.containsKey(player.getUniqueId())) return true;

        return false;
    }

    public void removePlayer(Player player)
    {
        _attackedPlayers.remove(player.getUniqueId());
    }

    public int getPlayerBlockTime(Player player)
    {
        return _attackedPlayers.getOrDefault(player.getUniqueId(), 0);
    }
}
