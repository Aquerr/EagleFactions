package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimContext;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import org.spongepowered.api.scheduler.ScheduledTask;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class DelayedClaimTask implements EagleFactionsConsumerTask<ScheduledTask>
{
    private final MessageService messageService;
    private final boolean requireStayingInChunk;
    private final Runnable claimWithStrategy;
    private final ClaimContext claimContext;

    private final int claimDelay;
    private int currentWaitSeconds = 0;

    boolean hasDisplayedStartMessage = false;

    public DelayedClaimTask(MessageService messageService,
                            boolean requireStayingInChunk,
                            int claimDelay,
                            Runnable claimWithStrategy,
                            ClaimContext claimContext)
    {
        this.messageService = messageService;
        this.requireStayingInChunk = requireStayingInChunk;
        this.claimDelay = claimDelay;
        this.claimWithStrategy = claimWithStrategy;
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
            claimWithStrategy.run();
            task.cancel();
        }
        else
        {
            claimContext.getServerPlayer().sendActionBar(PluginInfo.PLUGIN_PREFIX.append(text(currentWaitSeconds, WHITE)));
            currentWaitSeconds++;
        }
    }
}
