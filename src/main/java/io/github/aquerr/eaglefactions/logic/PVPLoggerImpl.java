package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.PVPLoggerConfig;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class PVPLoggerImpl implements PVPLogger
{
    private static PVPLogger INSTANCE = null;

    private final PVPLoggerConfig pvpLoggerConfig;
    private final MessageService messageService;
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
        this.messageService = plugin.getMessageService();
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

        final UUID playerUUID = player.uniqueId();

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
            final Scoreboard scoreboard = Optional.ofNullable(player.scoreboard()).orElse(Scoreboard.builder().build());
            final Objective objective = createPVPLoggerObjective(objectiveId);
            scoreboard.addObjective(objective);
            scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
            player.setScoreboard(scoreboard);
            pvpLoggerObjective.setObjective(objective);
        }

        this.playerPVPLoggerObjectives.put(playerUUID, pvpLoggerObjective);

        player.sendMessage(messageService.resolveMessageWithPrefix("pvplogger.pvp-logger-has-truned-on-you-will-die-if-you-disconnect", getBlockTime()));

        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(task ->
                {
                    if (this.playerPVPLoggerObjectives.containsKey(playerUUID))
                    {
                        PVPLoggerObjective loggerObjective = this.playerPVPLoggerObjectives.get(playerUUID);
                        if (loggerObjective.getSeconds() <= 0)
                        {
                            player.sendMessage(messageService.resolveMessageWithPrefix("pvplogger.pvp-logger-has-turned-off-you-can-disconnect-sefely"));
                            removePlayer(player);
                            task.cancel();
                            return;
                        }

                        loggerObjective.setSeconds(loggerObjective.getSeconds() - 1);

                        if(shouldDisplayInScoreboard)
                        {
                            final Optional<ServerPlayer> optionalPlayer = Sponge.server().player(playerUUID);
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

    private void createOrUpdatePVPLoggerObjective(ServerPlayer player, PVPLoggerObjective pvpLoggerObjective)
    {
        Objective objective = pvpLoggerObjective.getObjective();
        if (objective == null)
        {
            final int objectiveId = getNextFreeId(1);
            objective = createPVPLoggerObjective(objectiveId);
            player.scoreboard().addObjective(objective);
            player.scoreboard().updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
        }
        Score pvpTimerScore = objective.findOrCreateScore(Component.text("Time:"));
        pvpTimerScore.setScore(pvpLoggerObjective.getSeconds());
    }

    @Override
    public synchronized boolean isPlayerBlocked(final ServerPlayer player)
    {
        return this.playerPVPLoggerObjectives.containsKey(player.uniqueId());
    }

    @Override
    public synchronized void removePlayer(final ServerPlayer player)
    {
        if (!isPlayerBlocked(player))
            return;

        //Remove PVPLoggerImpl objective
        Scoreboard scoreboard = player.scoreboard();
        PVPLoggerObjective pvpLoggerObjective = this.playerPVPLoggerObjectives.get(player.uniqueId());
        scoreboard.removeObjective(pvpLoggerObjective.getObjective());
        this.playerPVPLoggerObjectives.remove(player.uniqueId());
    }

    @Override
    public synchronized int getPlayerBlockTime(final ServerPlayer player)
    {
        return Optional.ofNullable(this.playerPVPLoggerObjectives.get(player.uniqueId()))
                .map(PVPLoggerObjective::getSeconds)
                .orElse(0);
    }

    private Objective createPVPLoggerObjective(int objectiveId)
    {
        return Objective.builder()
                .name(PVPLOGGER_OBJECTIVE_NAME + objectiveId)
                .displayName(Component.text("===", WHITE).append(Component.text("PVP-LOGGER", RED)).append(Component.text("===", WHITE)))
                .criterion(Criteria.DUMMY)
                .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                .build();
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
