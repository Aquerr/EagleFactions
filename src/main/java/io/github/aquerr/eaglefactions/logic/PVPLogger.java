package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PVPLogger
{
    private Map<UUID, Integer> _attackedPlayers;
    private boolean _isActive;
    private int _blockTime;
    private List<String> _blockedCommandsDuringFight;

    public PVPLogger()
    {
        _isActive = MainLogic.isPVPLoggerActive();

        if (_isActive)
        {
            _attackedPlayers = new HashMap<>();
            _blockTime = MainLogic.getPVPLoggerTime();
            _blockedCommandsDuringFight = MainLogic.getBlockedCommandsDuringFight();
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

        if (_attackedPlayers.containsKey(player.getUniqueId()))
        {
            _attackedPlayers.replace(player.getUniqueId(), getBlockTime());
        }
        else
        {
            _attackedPlayers.put(player.getUniqueId(), getBlockTime());
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "PVPLogger has turned on! You will die if you disconnect in " + getBlockTime() + "s!"));

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
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "PVPLogger has turned off for you! You can now disconnect safely."));
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
            }).submit(EagleFactions.getEagleFactions());
        }
    }

    public boolean isPlayerBlocked(Player player)
    {
        if (_attackedPlayers.containsKey(player.getUniqueId())) return true;

        return false;
    }

    public void removePlayer(Player player)
    {
        if (_attackedPlayers.containsKey(player.getUniqueId()))
        {
            _attackedPlayers.remove(player.getUniqueId());
        }
    }

    public int getPlayerBlockTime(Player player)
    {
        return _attackedPlayers.getOrDefault(player.getUniqueId(), 0);
    }
}
