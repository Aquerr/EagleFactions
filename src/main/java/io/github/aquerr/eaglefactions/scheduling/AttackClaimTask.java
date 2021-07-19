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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class AttackClaimTask implements EagleFactionsConsumerTask<ScheduledTask>
{
    int seconds = 1;

    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;
    private final AttackLogic attackLogic;
    private final Vector3i attackedChunk;
    private final ServerPlayer player;

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
            this.player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.ATTACK_ON_CLAIM_HAS_BEEN_CANCELLED, NamedTextColor.RED)));
            task.cancel();
        }

        if (!this.attackedChunk.equals(this.player.location().chunkPosition()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MOVED_FROM_THE_CHUNK, NamedTextColor.RED)));
            task.cancel();
        }

        if(this.seconds >= this.factionsConfig.getAttackTime())
        {
            final Optional<Faction> optionalChunkFaction = factionLogic.getFactionByChunk(player.world().uniqueId(), attackedChunk);
            if (!optionalChunkFaction.isPresent())
            {
                task.cancel();
                return;
            }

            final Faction chunkFaction = optionalChunkFaction.get();
            this.attackLogic.informAboutDestroying(chunkFaction, player.serverLocation());
            this.player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.CLAIM_DESTROYED, NamedTextColor.GREEN)));

            final Claim claim = new Claim(player.world().uniqueId(), attackedChunk);
            factionLogic.destroyClaim(chunkFaction, claim);
            task.cancel();
        }
        else
        {
            this.player.sendActionBar(MessageLoader.parseMessage(Messages.CLAIM_WILL_BE_DESTROYED_IN_SECONDS, NamedTextColor.AQUA, ImmutableMap.of(Placeholders.NUMBER, Component.text(seconds, NamedTextColor.GOLD))));
            this.seconds++;
        }
    }
}
