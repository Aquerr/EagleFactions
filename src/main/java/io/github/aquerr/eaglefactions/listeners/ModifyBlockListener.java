package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;

public class ModifyBlockListener extends AbstractListener
{
    public ModifyBlockListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onBlockModify(ChangeBlockEvent.Modify event)
    {
        if(event.getContext().containsKey(EventContextKeys.OWNER)
            && event.getContext().get(EventContextKeys.OWNER).isPresent()
            && event.getContext().get(EventContextKeys.OWNER).get() instanceof Player)
        {
            Player player = (Player) event.getContext().get(EventContextKeys.OWNER).get();
            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                if(!super.getPlugin().getProtectionManager().canInteract(transaction.getFinal().getLocation().get(), player.getWorld(), player))
                    event.setCancelled(true);
            }
        }
    }
}
