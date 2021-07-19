package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.PVPLoggerConfig;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PVPLoggerImpl implements PVPLogger
{
    private static PVPLogger INSTANCE = null;

    private final PVPLoggerConfig pvpLoggerConfig;
    private Map<UUID, Integer> attackedPlayers;
    private Map<UUID, Integer> playersIdTaskMap;
    private boolean isActive;
    private int blockTime;
    private boolean shouldDisplayInScoreboard;
    private Set<String> blockedCommandsDuringFight;

    private final String PVPLOGGER_OBJECTIVE_NAME = "PVPLogger";

    public static PVPLogger getInstance(final EagleFactions plugin)
    {
        if (INSTANCE == null)
            return new PVPLoggerImpl(plugin);
        else return INSTANCE;
    }

    private PVPLoggerImpl(final EagleFactions plugin)
    {
        pvpLoggerConfig = plugin.getConfiguration().getPvpLoggerConfig();
        isActive = pvpLoggerConfig.isPVPLoggerActive();

        if (isActive)
        {
            attackedPlayers = new HashMap<>();
            playersIdTaskMap = new HashMap<>();
            blockTime = pvpLoggerConfig.getPVPLoggerBlockTime();
            blockedCommandsDuringFight = pvpLoggerConfig.getBlockedCommandsDuringFight();
            shouldDisplayInScoreboard = pvpLoggerConfig.shouldDisplayPvpLoggerInScoreboard();
        }
    }

    @Override
    public boolean isActive()
    {
        return this.isActive;
    }

    @Override
    public int getBlockTime()
    {
        return this.blockTime;
    }

    @Override
    public boolean shouldBlockCommand(final ServerPlayer player, final String command)
    {
        if (!isPlayerBlocked(player))
            return false;

        String usedCommand = command;
        if (command.charAt(0) == '/') //TODO: This is possibly not required... Need to check this.
        {
            usedCommand = command.substring(1);
        }

        usedCommand = usedCommand.toLowerCase();

        for (String blockedCommand : blockedCommandsDuringFight)
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

        return false;
    }

    //TODO: Go through this code and try to improve it.
    @Override
    public synchronized void addOrUpdatePlayer(final ServerPlayer player)
    {
        if(!isActive())
            return;

        //Update player's time if player is already blocked.
        if (attackedPlayers.containsKey(player.uniqueId()) && playersIdTaskMap.containsKey(player.uniqueId()))
        {
            attackedPlayers.replace(player.uniqueId(), getBlockTime());
            return;
        }

        if (shouldDisplayInScoreboard)
        {
            final Scoreboard scoreboard = Scoreboard.builder().build();
            final Objective objective = createPVPLoggerObjective();
            scoreboard.addObjective(objective);
            scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
            player.setScoreboard(scoreboard);
        }

        attackedPlayers.put(player.uniqueId(), getBlockTime());
        playersIdTaskMap.put(player.uniqueId(), getNewTaskId(1));
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.PVPLOGGER_HAS_TURNED_ON + " " + Messages.YOU_WILL_DIE_IF_YOU_DISCONNECT_IN + " " + getBlockTime() + " " + Messages.SECONDS + "!", NamedTextColor.RED)));

        Sponge.asyncScheduler().submit(Task.builder()
                .interval(1, TimeUnit.SECONDS)
                .execute(task ->
                {
                    if (attackedPlayers.containsKey(player.uniqueId()))
                    {
                        int seconds = attackedPlayers.get(player.uniqueId());

                        if (seconds <= 0)
                        {
                            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.PVPLOGGER_HAS_TURNED_OFF + " " + Messages.YOU_CAN_NOW_DISCONNECT_SAFELY, NamedTextColor.GREEN)));
                            removePlayer(player);
                            task.cancel();
                            return;
                        }

                        attackedPlayers.replace(player.uniqueId(), seconds - 1);
                        if(shouldDisplayInScoreboard)
                        {
                            Scoreboard scoreboard = player.scoreboard();
                            Optional<Objective> optionalObjective = scoreboard.objective(PVPLOGGER_OBJECTIVE_NAME + "-" + playersIdTaskMap.get(player.uniqueId()));
                            if (!optionalObjective.isPresent())
                            {
                                Objective objective = createPVPLoggerObjective();
                                scoreboard.addObjective(objective);
                                scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
                                optionalObjective = Optional.of(objective);
                            }
                            Score pvpTimer = optionalObjective.get().findOrCreateScore(Component.text("Time:"));
                            pvpTimer.setScore(seconds - 1);
                        }
                    }
                    else
                    {
                        task.cancel();
                    }
                })
                .build());
    }

    @Override
    public synchronized boolean isPlayerBlocked(final ServerPlayer player)
    {
        return attackedPlayers.containsKey(player.uniqueId());
    }


    @Override
    public synchronized void removePlayer(final ServerPlayer player)
    {
        if (!isPlayerBlocked(player))
            return;

        //Remove PVPLoggerImpl objective
        Scoreboard scoreboard = player.scoreboard();
        Optional<Objective> pvploggerObjective = scoreboard.objective(PVPLOGGER_OBJECTIVE_NAME + "-" + this.playersIdTaskMap.get(player.uniqueId()));
        pvploggerObjective.ifPresent(scoreboard::removeObjective);
        attackedPlayers.remove(player.uniqueId());
        playersIdTaskMap.remove(player.uniqueId());
    }

    @Override
    public synchronized int getPlayerBlockTime(final ServerPlayer player)
    {
        return attackedPlayers.getOrDefault(player.uniqueId(), 0);
    }

    private Integer getNewTaskId(int preferredId)
    {
        if(this.playersIdTaskMap.containsValue(preferredId))
        {
            return getNewTaskId(preferredId + 1);
        }

        return preferredId;
    }

    private Objective createPVPLoggerObjective()
    {
        return Objective.builder()
                .name(PVPLOGGER_OBJECTIVE_NAME + "-" + getNewTaskId(1))
                .displayName(Component.text("===", NamedTextColor.WHITE).append(Component.text("PVP-LOGGER", NamedTextColor.RED)).append(Component.text("===", NamedTextColor.WHITE)))
                .criterion(Criteria.DUMMY)
                .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                .build();
    }
}
