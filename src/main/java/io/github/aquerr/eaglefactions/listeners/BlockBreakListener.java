package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class BlockBreakListener extends AbstractListener
{
    private final ProtectionManager protectionManager;

    public BlockBreakListener(final EagleFactions plugin)
    {
        super(plugin);
        this.protectionManager = plugin.getProtectionManager();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreak(ChangeBlockEvent.All event)
    {
        if (isTriggeredByCommandBlock(event))
            return;

        boolean isPlayerBreak = event.context().containsKey(EventContextKeys.PLAYER_BREAK);
        for (BlockTransaction blockTransaction : event.transactions())
        {
            if (isPlayerBreak || blockTransaction.operation() == Operations.BREAK.get())
            {
                if (shouldCancelEvent(event, blockTransaction))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private boolean shouldCancelEvent(ChangeBlockEvent.All event, BlockTransaction blockTransaction)
    {
        User user = getUserFromEvent(event).orElse(null);
        printDebugMessageForUser(user, blockTransaction.finalReplacement(), event);

        if (user == null)
        {
            if (!this.protectionManager.canBreak(blockTransaction.original()).hasAccess())
            {
                return true;
            }
        }
        else
        {
            if (!this.protectionManager.canBreak(blockTransaction.original(), user, true).hasAccess())
            {
                return true;
            }
        }
        return false;
    }
}
