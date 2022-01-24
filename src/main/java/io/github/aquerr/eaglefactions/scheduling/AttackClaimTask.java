package io.github.aquerr.eaglefactions.scheduling;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class AttackClaimTask implements EagleFactionsConsumerTask<ScheduledTask>
{
    int seconds = 1;

    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;
    private final AttackLogic attackLogic;
    private final Vector3i attackedChunk;
    private final ServerPlayer player;
    private BossBar attackTimeBossBar;

    public AttackClaimTask(final FactionsConfig factionsConfig, final FactionLogic factionLogic, final AttackLogic attackLogic, final ServerPlayer player, final Vector3i attackedChunk)
    {
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
            cancelTask(player, task, PluginInfo.ERROR_PREFIX.append(text(Messages.ATTACK_ON_CLAIM_HAS_BEEN_CANCELLED, RED)));
            return;
        }

        if (!this.attackedChunk.equals(this.player.serverLocation().chunkPosition()))
        {
            cancelTask(player, task, PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MOVED_FROM_THE_CHUNK, RED)));
            return;
        }

        if(this.seconds >= this.factionsConfig.getAttackTime())
        {
            final Optional<Faction> optionalChunkFaction = factionLogic.getFactionByChunk(player.world().uniqueId(), attackedChunk);
            if (!optionalChunkFaction.isPresent())
            {
                cancelTask(player, task, PluginInfo.PLUGIN_PREFIX.append(text("Chunk is already unclaimed!", GREEN)));
                return;
            }

            final Faction chunkFaction = optionalChunkFaction.get();
            this.attackLogic.informAboutDestroying(chunkFaction, player.serverLocation());

            final Claim claim = new Claim(player.world().uniqueId(), attackedChunk);
            factionLogic.destroyClaim(chunkFaction, claim);
            cancelTask(player, task, PluginInfo.PLUGIN_PREFIX.append(text(Messages.CLAIM_DESTROYED, GREEN)));
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
                this.attackTimeBossBar = BossBar.bossBar(text("Chunk attack"),
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
            this.player.sendActionBar(MessageLoader.parseMessage(Messages.CLAIM_WILL_BE_DESTROYED_IN_SECONDS, AQUA, ImmutableMap.of(Placeholders.NUMBER, text(seconds, GOLD))));
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
