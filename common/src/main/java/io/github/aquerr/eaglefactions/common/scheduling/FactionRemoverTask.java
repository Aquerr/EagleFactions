package io.github.aquerr.eaglefactions.common.scheduling;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class FactionRemoverTask implements EagleFactionsRunnableTask
{
    private final FactionLogic factionLogic;
    private final FactionsConfig factionsConfig;

    public FactionRemoverTask(final EagleFactions plugin)
    {
        this.factionLogic = plugin.getFactionLogic();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public void run()
    {
        final long maxInactiveTimeInSeconds = this.factionsConfig.getMaxInactiveTime();
        final Map<String, Faction> factionsList = new HashMap<>(this.factionLogic.getFactions());
        final boolean shouldNotifyWhenRemoved = this.factionsConfig.shouldNotifyWhenFactionRemoved();
        final boolean shouldRegenerateWhenRemoved = this.factionsConfig.shouldRegenerateChunksWhenFactionRemoved();
        for(Map.Entry<String, Faction> factionEntry : factionsList.entrySet())
        {
            if(factionLogic.hasOnlinePlayers(factionEntry.getValue()))
                continue;

            if(factionEntry.getValue().isSafeZone() || factionEntry.getValue().isWarZone())
                continue;

            final Duration inactiveTime = Duration.between(factionEntry.getValue().getLastOnline(), Instant.now());
            if(inactiveTime.getSeconds() < maxInactiveTimeInSeconds)
                continue;

            final boolean isCancelled = EventRunner.runFactionDisbandEvent(null, factionEntry.getValue());
            if (isCancelled)
                continue;

            boolean didSucceed = this.factionLogic.disbandFaction(factionEntry.getValue().getName());

            if (didSucceed)
            {
                if (shouldNotifyWhenRemoved)
                {
                    Sponge.getServer().getBroadcastChannel().send(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_BEEN_REMOVED_DUE_TO_INACTIVITY_TIME, TextColors.RED, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, factionEntry.getKey())))));
                }

                if (shouldRegenerateWhenRemoved)
                {
                    for (Claim claim : factionEntry.getValue().getClaims())
                    {
                        Sponge.getServer().getWorld(claim.getWorldUUID()).ifPresent(world -> world.regenerateChunk(claim.getChunkPosition()));
                    }
                }
            }
        }
    }
}
