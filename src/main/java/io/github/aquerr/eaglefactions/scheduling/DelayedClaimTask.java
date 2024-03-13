package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.exception.CouldNotClaimException;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimContext;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimStrategy;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.events.EventRunner;
import org.spongepowered.api.scheduler.ScheduledTask;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class DelayedClaimTask implements EagleFactionsConsumerTask<ScheduledTask>
{
    private final MessageService messageService;
    private final boolean requireStayingInChunk;
    private final ClaimStrategy claimStrategy;
    private final ClaimContext claimContext;

    private final int claimDelay;
    private int currentWaitSeconds = 0;

    boolean hasDisplayedStartMessage = false;

    public DelayedClaimTask(MessageService messageService,
                            boolean requireStayingInChunk,
                            int claimDelay,
                            ClaimStrategy claimStrategy,
                            ClaimContext claimContext)
    {
        this.messageService = messageService;
        this.requireStayingInChunk = requireStayingInChunk;
        this.claimDelay = claimDelay;
        this.claimStrategy = claimStrategy;
        this.claimContext = claimContext;
    }


    @Override
    public void accept(ScheduledTask task)
    {
        if (requireStayingInChunk)
        {
            if (!hasDisplayedStartMessage)
            {
                hasDisplayedStartMessage = true;
                claimContext.getServerPlayer().sendMessage(messageService.resolveMessageWithPrefix("command.claim.stay-in-the-chunk-for-number-of-seconds-to-claim-it", claimDelay));
            }

            if (!claimContext.getServerLocation().chunkPosition().equals(claimContext.getServerPlayer().location().chunkPosition()))
            {
                claimContext.getServerPlayer().sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.claim.you-moved-from-chunk")));
                task.cancel();
            }
        }

        if(currentWaitSeconds >= claimDelay)
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

            task.cancel();
        }
        else
        {
            claimContext.getServerPlayer().sendActionBar(PluginInfo.PLUGIN_PREFIX.append(text(currentWaitSeconds, WHITE)));
            currentWaitSeconds++;
        }
    }
}
