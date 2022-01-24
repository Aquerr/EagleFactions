package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ClaimDelayTask implements EagleFactionsConsumerTask<ScheduledTask>
{
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
        this.claimDelay = this.factionsConfig.getClaimDelay();
        this.shouldClaimByItems = this.factionsConfig.shouldClaimByItems();
    }

    @Override
    public void accept(ScheduledTask task)
    {
        if(!chunkPosition.equals(player.location().chunkPosition()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MOVED_FROM_THE_CHUNK, RED)));
            task.cancel();
        }

        if(currentWaitSeconds >= claimDelay)
        {
            final Optional<Faction> optionalFaction = this.factionLogic.getFactionByPlayerUUID(player.uniqueId());
            if(!optionalFaction.isPresent())
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, RED)));
                task.cancel();
            }

            if(shouldClaimByItems)
            {
                boolean didSucceed = this.factionLogic.addClaimByItems(player, optionalFaction.get(), player.world().uniqueId(), chunkPosition);
                if(didSucceed)
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.LAND + " ")).append(text(chunkPosition.toString(), GOLD)).append(text(" " + Messages.HAS_BEEN_SUCCESSFULLY + " ", WHITE)).append(text(Messages.CLAIMED, GOLD)).append(text("!", WHITE)));
                else
                    player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY, RED)));
            }
            else
            {
                factionLogic.addClaim(optionalFaction.get(), new Claim(player.world().uniqueId(), chunkPosition));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.LAND + " ")).append(text(chunkPosition.toString(), GOLD)).append(text(" " + Messages.HAS_BEEN_SUCCESSFULLY + " ", WHITE)).append(text(Messages.CLAIMED, GOLD)).append(text("!", WHITE)));
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
