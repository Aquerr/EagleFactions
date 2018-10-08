package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class PlayerBlockPlaceListener extends AbstractListener
{
    public PlayerBlockPlaceListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player)
    {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            if(!super.getPlugin().getProtectionManager().canPlace(transaction.getFinal().getLocation().get(), player.getWorld(), player))
                event.setCancelled(true);
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent event)
    {
        if(event.getContext().containsKey(EventContextKeys.OWNER)
                && event.getContext().get(EventContextKeys.OWNER).isPresent()
                && event.getContext().get(EventContextKeys.OWNER).get() instanceof Player
                && event.getContext().containsKey(EventContextKeys.SPAWN_TYPE)
                && event.getContext().get(EventContextKeys.SPAWN_TYPE).isPresent()
                && event.getContext().get(EventContextKeys.SPAWN_TYPE).get() == SpawnTypes.PLACEMENT)
        {
            Player player = (Player) event.getContext().get(EventContextKeys.OWNER).get();
            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                if(!super.getPlugin().getProtectionManager().canPlace(transaction.getFinal().getLocation().get(), player.getWorld(), player))
                    event.setCancelled(true);
            }
        }

        if(event.getContext().containsKey(EventContextKeys.OWNER)
                && event.getContext().get(EventContextKeys.OWNER).isPresent()
                && event.getContext().get(EventContextKeys.OWNER).get() instanceof Player)
        {
            Player player = (Player) event.getContext().get(EventContextKeys.OWNER).get();
            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                if(!super.getPlugin().getProtectionManager().canPlace(transaction.getFinal().getLocation().get(), player.getWorld(), player))
                    event.setCancelled(true);
            }
        }
    }
}
