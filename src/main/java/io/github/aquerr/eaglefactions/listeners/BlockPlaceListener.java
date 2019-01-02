package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Piston;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class BlockPlaceListener extends AbstractListener
{
    public BlockPlaceListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onBlockPlace(ChangeBlockEvent.Place event)
    {
        final Object source = event.getSource();
        if(source instanceof Piston)
            return;

        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
        }

        if(user instanceof Player)
        {
            Player player = (Player) user;
            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                if(!super.getPlugin().getProtectionManager().canPlace(transaction.getFinal().getLocation().get(), player))
                    event.setCancelled(true);
            }
        }
        else if(user == null)
        {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                //Block fire from thunder
                if(transaction.getFinal().getState().getType() == BlockTypes.FIRE && super.getPlugin().getFactionLogic().getFactionByChunk(transaction.getFinal().getWorldUniqueId(), transaction.getFinal().getLocation().get().getChunkPosition()).isPresent())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent event)
    {
//        if(event.getContext().containsKey(EventContextKeys.OWNER)
//                && event.getContext().get(EventContextKeys.OWNER).isPresent()
//                && event.getContext().get(EventContextKeys.OWNER).get() instanceof Player
//                && event.getContext().containsKey(EventContextKeys.SPAWN_TYPE)
//                && event.getContext().get(EventContextKeys.SPAWN_TYPE).isPresent()
//                && event.getContext().get(EventContextKeys.SPAWN_TYPE).get() == SpawnTypes.PLACEMENT)
//        {
//            Player player = (Player) event.getContext().get(EventContextKeys.OWNER).get();
//            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
//            {
//                if(!super.getPlugin().getProtectionManager().canPlace(transaction.getFinal().getLocation().get(), player.getWorld(), player))
//                    event.setCancelled(true);
//            }
//        }
    }
}
