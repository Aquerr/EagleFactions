package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.PVPLoggerConfig;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

        final int objectiveId = getNewTaskId(1);
        attackedPlayers.put(playerUUID, getBlockTime());
        playersIdTaskMap.put(playerUUID, objectiveId);

        if (shouldDisplayInScoreboard)
        {
            final Scoreboard scoreboard = Scoreboard.builder().build();
            final Objective objective = createPVPLoggerObjective(objectiveId);
            scoreboard.addObjective(objective);
            scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
            player.setScoreboard(scoreboard);
        }

        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.PVPLOGGER_HAS_TURNED_ON + " " + Messages.YOU_WILL_DIE_IF_YOU_DISCONNECT_IN + " " + getBlockTime() + " " + Messages.SECONDS + "!"));

        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(task ->
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
                                createOrUpdatePVPLoggerObjective(scoreboard, playerUUID, seconds);
                            }
                        }
                    }
                    else
                    {
                        task.cancel();
                    }
                }, 0, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
    }

    private void createOrUpdatePVPLoggerObjective(Scoreboard scoreboard, UUID playerUUID, int seconds)
    {
        Optional<Objective> optionalObjective = scoreboard.getObjective(PVPLOGGER_OBJECTIVE_NAME + "-" + playersIdTaskMap.get(playerUUID));
        if (!optionalObjective.isPresent())
        {
            final int objectiveId = getNewTaskId(1);
            Objective objective = createPVPLoggerObjective(objectiveId);
            scoreboard.addObjective(objective);
            scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
            optionalObjective = Optional.of(objective);
        }
        Score pvpTimer = optionalObjective.get().getOrCreateScore(Text.of("Time:"));
        pvpTimer.setScore(seconds - 1);
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

    private int getNewTaskId(int preferredId)
    {
        if(this.playersIdTaskMap.containsValue(preferredId))
        {
            return getNewTaskId(preferredId + 1);
        }

        return preferredId;
    }

    private Objective createPVPLoggerObjective(int objectiveId)
    {
        return Objective.builder()
                .name(PVPLOGGER_OBJECTIVE_NAME + "-" + objectiveId)
                .displayName(Text.of(TextColors.WHITE, "===", TextColors.RED, "PVP-LOGGER", TextColors.WHITE, "==="))
                .criterion(Criteria.DUMMY)
                .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                .build();
    }
}
