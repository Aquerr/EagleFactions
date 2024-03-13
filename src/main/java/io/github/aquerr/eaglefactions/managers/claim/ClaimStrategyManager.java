package io.github.aquerr.eaglefactions.managers.claim;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.exception.CouldNotClaimException;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimContext;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimStrategy;
import io.github.aquerr.eaglefactions.api.managers.claim.DelayedClaimStrategy;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.scheduling.DelayedClaimTask;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;

import java.util.concurrent.TimeUnit;

public class ClaimStrategyManager
{
    private final MessageService messageService;

    public ClaimStrategyManager(MessageService messageService)
    {
        this.messageService = messageService;
    }

    public void claim(ClaimStrategy claimStrategy, ClaimContext claimContext)
    {
        if (claimStrategy instanceof DelayedClaimStrategy)
        {
            DelayedClaimStrategy delayedClaimStrategy = (DelayedClaimStrategy) claimStrategy;
            EagleFactionsScheduler.getInstance().scheduleWithDelayedInterval(new DelayedClaimTask(
                            messageService,
                            delayedClaimStrategy.isRequireStayingInChunk(),
                            delayedClaimStrategy.getDelay(),
                            claimStrategy,
                            claimContext),
                    1, TimeUnit.SECONDS,
                    1, TimeUnit.SECONDS);
        }
        else
        {
            try
            {
                claimStrategy.claim(claimContext);
                claimContext.getServerPlayer().sendMessage(messageService.resolveComponentWithMessage("command.claim.land-has-been-successfully-claimed", claimContext.getServerLocation().chunkPosition().toString()));
                EventRunner.runFactionClaimEventPost(claimContext.getServerPlayer(), claimContext.getFaction(), claimContext.getServerLocation().world(), claimContext.getServerLocation().chunkPosition());
            }
            catch (CouldNotClaimException e)
            {
                claimContext.getServerPlayer().sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.claim.could-not-claim-territory-with-reason", e.getMessage())));
            }
            catch (Exception e)
            {
                claimContext.getServerPlayer().sendMessage(messageService.resolveMessageWithPrefix("error.claim.could-not-claim-territory"));
                e.printStackTrace();
            }
        }
    }
}
