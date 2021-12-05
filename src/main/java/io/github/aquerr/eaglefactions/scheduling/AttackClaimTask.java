package io.github.aquerr.eaglefactions.scheduling;

import com.flowpowered.math.vector.Vector3i;
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
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class AttackClaimTask implements EagleFactionsConsumerTask<Task>
{
    int seconds = 1;

    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;
    private final AttackLogic attackLogic;
    private final Vector3i attackedChunk;
    private final Player player;
    private ServerBossBar attackTimeBossBar;

    public AttackClaimTask(final FactionsConfig factionsConfig, final FactionLogic factionLogic, final AttackLogic attackLogic, final Player player, final Vector3i attackedChunk)
    {
        this.factionsConfig = factionsConfig;
        this.factionLogic = factionLogic;
        this.attackLogic = attackLogic;
        this.player = player;
        this.attackedChunk = attackedChunk;
    }

    @Override
    public void accept(Task task)
    {
        if (this.player.health().get() <= 0)
        {
            cancelTask(player, task, Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ATTACK_ON_CLAIM_HAS_BEEN_CANCELLED));
        }

        if (!this.attackedChunk.equals(this.player.getLocation().getChunkPosition()))
        {
            cancelTask(player, task, Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MOVED_FROM_THE_CHUNK));
        }

        if(this.seconds >= this.factionsConfig.getAttackTime())
        {
            final Optional<Faction> optionalChunkFaction = factionLogic.getFactionByChunk(player.getWorld().getUniqueId(), attackedChunk);
            if (!optionalChunkFaction.isPresent())
            {
                cancelTask(player, task, Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Chunk is already unclaimed!"));
                return;
            }

            final Faction chunkFaction = optionalChunkFaction.get();
            this.attackLogic.informAboutDestroying(chunkFaction, player.getLocation());

            final Claim claim = new Claim(player.getWorld().getUniqueId(), attackedChunk);
            factionLogic.destroyClaim(chunkFaction, claim);
            cancelTask(player, task, Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.CLAIM_DESTROYED));
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
                this.attackTimeBossBar = ServerBossBar.builder()
                        .name(Text.of("Chunk attack"))
                        .color(BossBarColors.RED)
                        .overlay(BossBarOverlays.NOTCHED_20)
                        .percent(calculatePercentage(requiredSeconds, seconds))
                        .playEndBossMusic(false)
                        .visible(true)
                        .build();
                this.attackTimeBossBar.addPlayer(player);
            }
            else
            {
                this.attackTimeBossBar.setPercent(calculatePercentage(requiredSeconds, seconds));
            }
        }
        else
        {
            this.player.sendMessage(ChatTypes.ACTION_BAR, MessageLoader.parseMessage(Messages.CLAIM_WILL_BE_DESTROYED_IN_SECONDS, TextColors.AQUA, ImmutableMap.of(Placeholders.NUMBER, Text.of(TextColors.GOLD, seconds))));
        }
    }

    private float calculatePercentage(int maxValue, int value)
    {
        return (float)value / maxValue;
    }

    private void cancelTask(Player player, Task task, Text reasonMessage)
    {
        if (this.factionsConfig.shouldShowAttackInBossBar())
        {
            this.attackTimeBossBar.removePlayer(player);
            this.attackTimeBossBar.setVisible(false);
        }

        this.player.sendMessage(reasonMessage);
        task.cancel();
    }
}
