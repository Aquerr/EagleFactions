package io.github.aquerr.eaglefactions.common.scheduling;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ClaimDelayTask implements EagleFactionsConsumerTask<Task>
{
    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic = EagleFactionsPlugin.getPlugin().getFactionLogic();
    private final Player player;
    private final Vector3i chunkPosition;
    private final int claimDelay;
    private final boolean shouldClaimByItems;

    private int currentWaitSeconds = 0;

    public ClaimDelayTask(Player player, Vector3i chunkPosition)
    {
        this.player = player;
        this.chunkPosition = chunkPosition;
        this.factionsConfig = EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig();
        this.claimDelay = this.factionsConfig.getClaimDelay();
        this.shouldClaimByItems = this.factionsConfig.shouldClaimByItems();
    }

    @Override
    public void accept(Task task)
    {
        if(!chunkPosition.equals(player.getLocation().getChunkPosition()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MOVED_FROM_THE_CHUNK));
            task.cancel();
        }

        if(currentWaitSeconds >= claimDelay)
        {
            final Optional<Faction> optionalFaction = this.factionLogic.getFactionByPlayerUUID(player.getUniqueId());
            if(!optionalFaction.isPresent())
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                task.cancel();
            }

            if(shouldClaimByItems)
            {
                boolean didSucceed = this.factionLogic.addClaimByItems(player, optionalFaction.get(), player.getWorld().getUniqueId(), chunkPosition);
                if(didSucceed)
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunkPosition.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
                else
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY));
            }
            else
            {
                factionLogic.addClaim(optionalFaction.get(), new Claim(player.getWorld().getUniqueId(), chunkPosition));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunkPosition.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
            }
            task.cancel();
        }
        else
        {
            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RESET, currentWaitSeconds));
            currentWaitSeconds++;
        }
    }
}
