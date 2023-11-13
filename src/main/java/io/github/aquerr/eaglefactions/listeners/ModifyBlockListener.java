package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public class ModifyBlockListener extends AbstractListener
{
    private final ProtectionConfig protectionConfig;
    private final ProtectionManager protectionManager;

    public ModifyBlockListener(EagleFactions plugin)
    {
        super(plugin);
        this.protectionManager = plugin.getProtectionManager();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockModify(ChangeBlockEvent.All event)
    {
        if (isTriggeredByCommandBlock(event))
            return;

        for (BlockTransaction blockTransaction : event.transactions())
        {
            if (blockTransaction.operation() == Operations.MODIFY.get())
            {
                if (isFireCause(event) && shouldCancelFire(blockTransaction))
                {
                    event.setCancelled(true);
                    return;
                }
                else if (shouldCancelEvent(event, blockTransaction))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private boolean isFireCause(ChangeBlockEvent.All event)
    {
        return event.cause().first(LocatableBlock.class)
                .map(LocatableBlock::blockState)
                .map(BlockState::type)
                .map(BlockTypes.FIRE.get()::equals)
                .orElse(false);
    }

    private boolean shouldCancelFire(BlockTransaction blockTransaction)
    {
        ServerLocation serverLocation = blockTransaction.finalReplacement().location().orElse(null);
        if (serverLocation != null)
        {
            Optional<Faction> optionalChunkFaction = this.getPlugin().getFactionLogic().getFactionByChunk(serverLocation.world().uniqueId(), serverLocation.chunkPosition());
            if (this.protectionConfig.getSafeZoneWorldNames().contains(WorldUtil.getPlainWorldName(serverLocation.world()))
                    && !super.getPlugin().getFactionLogic().getFactionByName(EagleFactionsPlugin.SAFE_ZONE_NAME).getProtectionFlagValue(ProtectionFlagType.FIRE_SPREAD))
            {
                return true;
            }
            else return (optionalChunkFaction.isPresent()) && !optionalChunkFaction.get().getProtectionFlagValue(ProtectionFlagType.FIRE_SPREAD);
        }
        return false;
    }

    private boolean shouldCancelEvent(ChangeBlockEvent.All event, BlockTransaction blockTransaction)
    {
        User user = getUserFromEvent(event).orElse(null);
        printDebugMessageForUser(user, blockTransaction.finalReplacement(), event);
        if (user == null)
        {
            return !this.protectionManager.canBreak(blockTransaction.original()).hasAccess();
        }
        else
        {
            return !this.protectionManager.canPlace(blockTransaction.finalReplacement(), user, true).hasAccess();
        }
    }
}
