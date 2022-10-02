package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.events.EventRunner;
import org.spongepowered.api.Sponge;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FactionRemoverTask implements EagleFactionsRunnableTask
{
    private final FactionLogic factionLogic;
    private final FactionsConfig factionsConfig;
    private final EagleFactionsScheduler scheduler = EagleFactionsScheduler.getInstance();
    private final MessageService messageService;

    public FactionRemoverTask(final EagleFactions plugin)
    {
        this.factionLogic = plugin.getFactionLogic();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
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
            if(faction.isSafeZone() || faction.isWarZone())
                continue;

            if(factionLogic.hasOnlinePlayers(faction))
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
                    Sponge.server().broadcastAudience().sendMessage(messageService.resolveComponentWithMessage("faction-remover.faction-has-been-removed-due-to-inactivity-time", faction.getName()));
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
