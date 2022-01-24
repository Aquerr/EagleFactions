package io.github.aquerr.eaglefactions.scheduling;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.Sponge;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class FactionRemoverTask implements EagleFactionsRunnableTask
{
    private final FactionLogic factionLogic;
    private final FactionsConfig factionsConfig;
    private final EagleFactionsScheduler scheduler = EagleFactionsScheduler.getInstance();

    public FactionRemoverTask(final EagleFactions plugin)
    {
        this.factionLogic = plugin.getFactionLogic();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public void run()
    {
        final long maxInactiveTimeInSeconds = this.factionsConfig.getMaxInactiveTime();
        final Map<String, Faction> factionsList = this.factionLogic.getFactions();
        final boolean shouldNotifyWhenRemoved = this.factionsConfig.shouldNotifyWhenFactionRemoved();
        final boolean shouldRegenerateWhenRemoved = this.factionsConfig.shouldRegenerateChunksWhenFactionRemoved();
        for(Faction faction : factionsList.values())
        {
            if(factionLogic.hasOnlinePlayers(faction))
                continue;

            if(faction.isSafeZone() || faction.isWarZone())
                continue;

            final Duration inactiveTime = Duration.between(faction.getLastOnline(), Instant.now());
            if(inactiveTime.getSeconds() < maxInactiveTimeInSeconds)
                continue;

            final boolean isCancelled = EventRunner.runFactionDisbandEventPre(null, faction, false, true);
            if (isCancelled)
                continue;

            boolean didSucceed = this.factionLogic.disbandFaction(faction.getName());

            if (didSucceed)
            {
                if (shouldNotifyWhenRemoved)
                {
                    Sponge.server().broadcastAudience().sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_HAS_BEEN_REMOVED_DUE_TO_INACTIVITY_TIME, RED, ImmutableMap.of(Placeholders.FACTION_NAME, text(faction.getName(), GOLD)))));
                }

                if (shouldRegenerateWhenRemoved)
                {
                    for (final Claim claim : faction.getClaims())
                    {
                        scheduler.scheduleWithDelay(new WorldRegenTask(claim), 0, TimeUnit.SECONDS);
                    }
                }
                EventRunner.runFactionDisbandEventPost(null, faction, false, true);
            }
        }
    }
}
