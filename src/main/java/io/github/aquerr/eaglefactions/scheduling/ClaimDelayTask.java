package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ClaimDelayTask implements EagleFactionsConsumerTask<ScheduledTask>
{
    private final MessageService messageService;
    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic = EagleFactionsPlugin.getPlugin().getFactionLogic();
    private final ServerPlayer player;
    private final Vector3i chunkPosition;
    private final int claimDelay;
    private final boolean shouldClaimByItems;

    private int currentWaitSeconds = 0;

    public ClaimDelayTask(ServerPlayer player, Vector3i chunkPosition)
    {
        this.player = player;
        this.chunkPosition = chunkPosition;
        this.factionsConfig = EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig();
        this.messageService = EagleFactionsPlugin.getPlugin().getMessageService();
        this.claimDelay = this.factionsConfig.getClaimDelay();
        this.shouldClaimByItems = this.factionsConfig.shouldClaimByItems();
    }

    @Override
    public void accept(ScheduledTask task)
    {
        if(!chunkPosition.equals(player.location().chunkPosition()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.claim.you-moved-from-chunk")));
            task.cancel();
        }

        if(currentWaitSeconds >= claimDelay)
        {
            final Optional<Faction> optionalFaction = this.factionLogic.getFactionByPlayerUUID(player.uniqueId());
            if(!optionalFaction.isPresent())
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY)));
                task.cancel();
            }

            if(shouldClaimByItems)
            {
                boolean didSucceed = this.factionLogic.addClaimByItems(player, optionalFaction.get(), player.world().uniqueId(), chunkPosition);
                if(didSucceed)
                    player.sendMessage(messageService.resolveComponentWithMessage("command.claim.land-has-been-successfully-claimed", chunkPosition.toString()));
                else
                    player.sendMessage(messageService.resolveComponentWithMessage("error.command.claim.not-enough-resources"));
            }
            else
            {
                factionLogic.addClaim(optionalFaction.get(), new Claim(player.world().uniqueId(), chunkPosition));
                player.sendMessage(messageService.resolveComponentWithMessage("command.claim.land-has-been-successfully-claimed", chunkPosition.toString()));
                EventRunner.runFactionClaimEventPost(player, optionalFaction.get(), player.world(), chunkPosition);
            }
            task.cancel();
        }
        else
        {
            player.sendActionBar(PluginInfo.PLUGIN_PREFIX.append(text(currentWaitSeconds, WHITE)));
            currentWaitSeconds++;
        }
    }
}
