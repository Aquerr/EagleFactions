package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class AttackClaimTask implements EagleFactionsConsumerTask<ScheduledTask>
{
    int seconds = 1;

    private final MessageService messageService;
    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;
    private final AttackLogic attackLogic;
    private final Vector3i attackedChunk;
    private final ServerPlayer player;
    private BossBar attackTimeBossBar;

    public AttackClaimTask(final MessageService messageService, final FactionsConfig factionsConfig, final FactionLogic factionLogic, final AttackLogic attackLogic, final ServerPlayer player, final Vector3i attackedChunk)
    {
        this.messageService = messageService;
        this.factionsConfig = factionsConfig;
        this.factionLogic = factionLogic;
        this.attackLogic = attackLogic;
        this.player = player;
        this.attackedChunk = attackedChunk;
    }

    @Override
    public void accept(ScheduledTask task)
    {
        if (this.player.health().get() <= 0)
        {
            cancelTask(player, task, PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("attack.cancelled")));
            return;
        }

        if (!this.attackedChunk.equals(this.player.serverLocation().chunkPosition()))
        {
            cancelTask(player, task, PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("attack.you-moved-from-chunk")));
            return;
        }

        if(this.seconds >= this.factionsConfig.getAttackTime())
        {
            final Optional<Faction> optionalChunkFaction = factionLogic.getFactionByChunk(player.world().uniqueId(), attackedChunk);
            if (!optionalChunkFaction.isPresent())
            {
                cancelTask(player, task, PluginInfo.PLUGIN_PREFIX.append(messageService.resolveComponentWithMessage("error.claim.place-does-not-belong-to-anyone")));
                return;
            }

            final Faction chunkFaction = optionalChunkFaction.get();
            this.attackLogic.informAboutDestroying(chunkFaction, player.serverLocation());

            final Claim claim = new Claim(player.world().uniqueId(), attackedChunk);
            factionLogic.destroyClaim(chunkFaction, claim);
            cancelTask(player, task, PluginInfo.PLUGIN_PREFIX.append(messageService.resolveComponentWithMessage("attack.claim-destroyed")));
        }
        else
        {
            showRemainingAttackSeconds(player, this.factionsConfig.getAttackTime(), seconds);
            this.seconds++;
        }
    }

    private void showRemainingAttackSeconds(Player player, int requiredSeconds, int seconds)
    {
        if (this.factionsConfig.shouldShowAttackInBossBar())
        {
            if(this.attackTimeBossBar == null)
            {
                this.attackTimeBossBar = BossBar.bossBar(messageService.resolveComponentWithMessage("attack.bar"),
                        calculatePercentage(requiredSeconds, seconds),
                        BossBar.Color.RED,
                        BossBar.Overlay.NOTCHED_20);
                player.showBossBar(this.attackTimeBossBar);
            }
            else
            {
                this.attackTimeBossBar.progress(calculatePercentage(requiredSeconds, seconds));
            }
        }
        else
        {
            this.player.sendActionBar(messageService.resolveComponentWithMessage("attack.claim-will-be-destroyed-in-seconds", seconds));
        }
    }

    private float calculatePercentage(int maxValue, int value)
    {
        return (float)value / maxValue;
    }

    private void cancelTask(ServerPlayer player, ScheduledTask task, Component reasonMessage)
    {
        if (this.factionsConfig.shouldShowAttackInBossBar())
        {
            player.hideBossBar(this.attackTimeBossBar);
            this.attackTimeBossBar = null;
        }

        this.player.sendMessage(reasonMessage);
        task.cancel();
    }
}
