package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.PVPLoggerConfig;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.messaging.Messages;
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
import java.util.concurrent.ConcurrentHashMap;
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
            attackedPlayers = new ConcurrentHashMap<>();
            playersIdTaskMap = new ConcurrentHashMap<>();
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
    public boolean shouldBlockCommand(final Player player, final String command)
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
    public synchronized void addOrUpdatePlayer(final Player player)
    {
        if(!isActive())
            return;

        final UUID playerUUID = player.getUniqueId();

        //Update player's time if player is already blocked.
        if (attackedPlayers.containsKey(playerUUID) && playersIdTaskMap.containsKey(playerUUID))
        {
            attackedPlayers.replace(playerUUID, getBlockTime());
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

        attackedPlayers.put(playerUUID, getBlockTime());
        playersIdTaskMap.put(playerUUID, getNewTaskId(1));
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.PVPLOGGER_HAS_TURNED_ON + " " + Messages.YOU_WILL_DIE_IF_YOU_DISCONNECT_IN + " " + getBlockTime() + " " + Messages.SECONDS + "!"));

        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.interval(1, TimeUnit.SECONDS).execute(task ->
        {
            if (attackedPlayers.containsKey(playerUUID))
            {
                int seconds = attackedPlayers.get(playerUUID);

                if (seconds <= 0)
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.PVPLOGGER_HAS_TURNED_OFF + " " + Messages.YOU_CAN_NOW_DISCONNECT_SAFELY));
                    removePlayer(player);
                    task.cancel();
                    return;
                }

                attackedPlayers.replace(playerUUID, seconds - 1);

                if(shouldDisplayInScoreboard)
                {
                    final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(playerUUID);
                    if (optionalPlayer.isPresent())
                    {
                        Scoreboard scoreboard = player.getScoreboard();
                        Optional<Objective> optionalObjective = scoreboard.getObjective(PVPLOGGER_OBJECTIVE_NAME + "-" + playersIdTaskMap.get(playerUUID));
                        if (!optionalObjective.isPresent())
                        {
                            Objective objective = createPVPLoggerObjective();
                            scoreboard.addObjective(objective);
                            scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
                            optionalObjective = Optional.of(objective);
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
        }).async().submit(EagleFactionsPlugin.getPlugin());
    }

    @Override
    public synchronized boolean isPlayerBlocked(final Player player)
    {
        return attackedPlayers.containsKey(player.getUniqueId());
    }


    @Override
    public synchronized void removePlayer(final Player player)
    {
        if (!isPlayerBlocked(player))
            return;

        //Remove PVPLoggerImpl objective
        Scoreboard scoreboard = player.getScoreboard();
        Optional<Objective> pvploggerObjective = scoreboard.getObjective(PVPLOGGER_OBJECTIVE_NAME + "-" + this.playersIdTaskMap.get(player.getUniqueId()));
        pvploggerObjective.ifPresent(scoreboard::removeObjective);
        attackedPlayers.remove(player.getUniqueId());
        playersIdTaskMap.remove(player.getUniqueId());
    }

    @Override
    public synchronized int getPlayerBlockTime(final Player player)
    {
        return attackedPlayers.getOrDefault(player.getUniqueId(), 0);
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
                .displayName(Text.of(TextColors.WHITE, "===", TextColors.RED, "PVP-LOGGER", TextColors.WHITE, "==="))
                .criterion(Criteria.DUMMY)
                .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                .build();
    }
}
