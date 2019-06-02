package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.config.IConfiguration;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PVPLogger
{
    private ConfigFields _configFields;
    private Map<UUID, Integer> _attackedPlayers;
    private Map<UUID, Integer> _playersIdTaskMap;
    private boolean _isActive;
    private int _blockTime;
    private boolean _shouldDisplayInScoreboard;
    private List<String> _blockedCommandsDuringFight;

    private final String PVPLOGGER_OBJECTIVE_NAME = "PVPLogger";

    public PVPLogger(IConfiguration configuration)
    {
        _configFields = configuration.getConfigFields();
        _isActive = _configFields.isPVPLoggerActive();

        if (_isActive)
        {
            _attackedPlayers = new HashMap<>();
            _playersIdTaskMap = new HashMap<>();
            _blockTime = _configFields.getPVPLoggerBlockTime();
            _blockedCommandsDuringFight = _configFields.getBlockedCommandsDuringFight();
            _shouldDisplayInScoreboard = _configFields.shouldDisplayPvpLoggerInScoreboard();
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
                _playersIdTaskMap.put(player.getUniqueId(), getNewTaskId(1));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, PluginMessages.PVPLOGGER_HAS_TURNED_ON + " " + PluginMessages.YOU_WILL_DIE_IF_YOU_DISCONNECT_IN + " " + getBlockTime() + " " + PluginMessages.SECONDS + "!"));

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
                                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.PVPLOGGER_HAS_TURNED_OFF + " " + PluginMessages.YOU_CAN_NOW_DISCONNECT_SAFELY));
                                removePlayer(player);
                                task.cancel();
                            }
                            else
                            {
                                _attackedPlayers.replace(player.getUniqueId(), seconds - 1);
                                if(_shouldDisplayInScoreboard)
                                {
                                    Scoreboard scoreboard = player.getScoreboard();
                                    Optional<Objective> optionalObjective = scoreboard.getObjective(PVPLOGGER_OBJECTIVE_NAME + "-" + _playersIdTaskMap.get(player.getUniqueId()));
                                    if(!optionalObjective.isPresent())
                                    {
                                        optionalObjective = Optional.of(Objective.builder().name(PVPLOGGER_OBJECTIVE_NAME + "-" + _playersIdTaskMap.get(player.getUniqueId())).displayName(Text.of(TextColors.WHITE, "===", TextColors.RED, "PVP-LOGGER", TextColors.WHITE, "===")).criterion(Criteria.DUMMY).objectiveDisplayMode(ObjectiveDisplayModes.INTEGER).build());
                                        scoreboard.addObjective(optionalObjective.get());
                                        scoreboard.updateDisplaySlot(optionalObjective.get(), DisplaySlots.SIDEBAR);
                                    }

                                    Score pvpTimer = optionalObjective.get().getOrCreateScore(Text.of("Time:"));
                                    pvpTimer.setScore(seconds - 1);
                                }
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

    private Integer getNewTaskId(int preferredId)
    {
        if(this._playersIdTaskMap.values().contains(preferredId))
        {
            return getNewTaskId(preferredId + 1);
        }

        return preferredId;
    }

    public boolean isPlayerBlocked(Player player)
    {
        return _attackedPlayers.containsKey(player.getUniqueId());
    }

    public void removePlayer(Player player)
    {
        synchronized(_attackedPlayers)
        {
            //Remove PVPLogger objective
            Scoreboard scoreboard = player.getScoreboard();
            Optional<Objective> pvploggerObjective = scoreboard.getObjective(PVPLOGGER_OBJECTIVE_NAME + "-" + this._playersIdTaskMap.get(player.getUniqueId()));
            if (pvploggerObjective.isPresent())
                scoreboard.removeObjective(pvploggerObjective.get());
            _attackedPlayers.remove(player.getUniqueId());
        }

        synchronized(_playersIdTaskMap)
        {
            _playersIdTaskMap.remove(player.getUniqueId());
        }
    }

    public int getPlayerBlockTime(Player player)
    {
        synchronized(_attackedPlayers)
        {
            return _attackedPlayers.getOrDefault(player.getUniqueId(), 0);
        }
    }
}
