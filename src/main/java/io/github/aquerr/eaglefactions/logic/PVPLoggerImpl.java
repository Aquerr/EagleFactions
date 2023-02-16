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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PVPLoggerImpl implements PVPLogger
{
    private static PVPLogger INSTANCE = null;

    private final PVPLoggerConfig pvpLoggerConfig;
    private Map<UUID, PVPLoggerObjective> playerPVPLoggerObjectives;

    private final boolean isActive;
    private int blockTime;
    protected boolean shouldDisplayInScoreboard;
    private Set<String> blockedCommandsDuringFight;

    private static final String PVPLOGGER_OBJECTIVE_NAME = "PVPLogger-";

    public static PVPLogger getInstance(final EagleFactions plugin)
    {
        if (INSTANCE == null)
            return new PVPLoggerImpl(plugin);
        else return INSTANCE;
    }

    private PVPLoggerImpl(final EagleFactions plugin)
    {
        this.pvpLoggerConfig = plugin.getConfiguration().getPvpLoggerConfig();
        this.isActive = pvpLoggerConfig.isPVPLoggerActive();

        if (this.isActive)
        {
            this.playerPVPLoggerObjectives = new ConcurrentHashMap<>();
            this.blockTime = pvpLoggerConfig.getPVPLoggerBlockTime();
            this.blockedCommandsDuringFight = pvpLoggerConfig.getBlockedCommandsDuringFight();
            this.shouldDisplayInScoreboard = pvpLoggerConfig.shouldDisplayPvpLoggerInScoreboard();
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
        if (playerPVPLoggerObjectives.containsKey(playerUUID) && playerPVPLoggerObjectives.containsKey(playerUUID))
        {
            playerPVPLoggerObjectives.get(playerUUID).setSeconds(getBlockTime());
            return;
        }


        final int objectiveId = getNextFreeId(1);
        PVPLoggerObjective pvpLoggerObjective = new PVPLoggerObjective(objectiveId, getBlockTime());

        if (shouldDisplayInScoreboard)
        {
            createOrUpdatePVPLoggerObjective(player, pvpLoggerObjective);
        }

        this.playerPVPLoggerObjectives.put(playerUUID, pvpLoggerObjective);

        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.PVPLOGGER_HAS_TURNED_ON + " " + Messages.YOU_WILL_DIE_IF_YOU_DISCONNECT_IN + " " + getBlockTime() + " " + Messages.SECONDS + "!"));

        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(task ->
                {
                    if (this.playerPVPLoggerObjectives.containsKey(playerUUID))
                    {
                        PVPLoggerObjective loggerObjective = this.playerPVPLoggerObjectives.get(playerUUID);
                        if (loggerObjective.getSeconds() <= 0)
                        {
                            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.PVPLOGGER_HAS_TURNED_OFF + " " + Messages.YOU_CAN_NOW_DISCONNECT_SAFELY));
                            removePlayer(player);
                            task.cancel();
                            return;
                        }

                        loggerObjective.setSeconds(loggerObjective.getSeconds() - 1);

                        if(shouldDisplayInScoreboard)
                        {
                            final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(playerUUID);
                            optionalPlayer.ifPresent(value -> createOrUpdatePVPLoggerObjective(value, loggerObjective));
                        }
                    }
                    else
                    {
                        task.cancel();
                    }
                }, 0, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
    }

    private int getNextFreeId(int preferredId)
    {
        for (final PVPLoggerObjective pvpLoggerObjective : this.playerPVPLoggerObjectives.values())
        {
            if (pvpLoggerObjective.getId() == preferredId)
                return getNextFreeId(preferredId + 1);
        }
        return preferredId;
    }

    private void createOrUpdatePVPLoggerObjective(Player player, PVPLoggerObjective pvpLoggerObjective)
    {
        Objective objective = pvpLoggerObjective.getObjective();
        if (objective == null)
        {
            final Scoreboard scoreboard = Optional.ofNullable(player.getScoreboard()).orElse(Scoreboard.builder().build());
            objective = findObjectiveInScoreBoard(scoreboard, pvpLoggerObjective.getId());
            if (objective == null)
            {
                final int objectiveId = getNextFreeId(1);
                objective = createPVPLoggerObjective(objectiveId);
                scoreboard.addObjective(objective);
                scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
                pvpLoggerObjective.setObjective(objective);
            }
            player.setScoreboard(scoreboard);
        }
        Score pvpTimerScore = objective.getOrCreateScore(Text.of("Time:"));
        pvpTimerScore.setScore(pvpLoggerObjective.getSeconds());
    }

    private Objective findObjectiveInScoreBoard(Scoreboard scoreboard, int objectiveId)
    {
        return scoreboard.getObjective(getPvploggerObjectiveName(objectiveId))
                .orElse(null);
    }

    @Override
    public synchronized boolean isPlayerBlocked(final Player player)
    {
        return this.playerPVPLoggerObjectives.containsKey(player.getUniqueId());
    }

    @Override
    public synchronized void removePlayer(final Player player)
    {
        Scoreboard scoreboard = player.getScoreboard();
        List<Objective> objectives = scoreboard.getObjectives().stream()
                .filter(objective -> objective.getName().startsWith(PVPLOGGER_OBJECTIVE_NAME))
                .collect(Collectors.toList());
        objectives.forEach(scoreboard::removeObjective);
        this.playerPVPLoggerObjectives.remove(player.getUniqueId());
    }

    @Override
    public synchronized int getPlayerBlockTime(final Player player)
    {
        return Optional.ofNullable(this.playerPVPLoggerObjectives.get(player.getUniqueId()))
                .map(PVPLoggerObjective::getSeconds)
                .orElse(0);
    }

    private Objective createPVPLoggerObjective(int objectiveId)
    {
        return Objective.builder()
                .name(getPvploggerObjectiveName(objectiveId))
                .displayName(Text.of(TextColors.WHITE, "===", TextColors.RED, "PVP-LOGGER", TextColors.WHITE, "==="))
                .criterion(Criteria.DUMMY)
                .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                .build();
    }

    private String getPvploggerObjectiveName(int objectiveId)
    {
        return PVPLOGGER_OBJECTIVE_NAME + objectiveId;
    }

    private static class PVPLoggerObjective
    {
        private final int id;
        private int seconds;
        private Objective objective;

        PVPLoggerObjective(int id, int startSeconds)
        {
            this.id = id;
            this.seconds = startSeconds;
        }

        public Objective getObjective()
        {
            return objective;
        }

        public void setObjective(Objective objective)
        {
            this.objective = objective;
        }

        public int getId()
        {
            return id;
        }

        public int getSeconds()
        {
            return seconds;
        }

        public void setSeconds(int seconds)
        {
            this.seconds = seconds;
        }
    }
}
