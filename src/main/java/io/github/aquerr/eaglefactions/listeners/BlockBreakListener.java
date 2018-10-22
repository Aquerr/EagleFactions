package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlockBreakListener extends AbstractListener
{
    public BlockBreakListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onBlockBreak(ChangeBlockEvent.Pre event)
    {
        if(event.getContext().containsKey(EventContextKeys.PLAYER_BREAK) || event.getContext().containsKey(EventContextKeys.FIRE_SPREAD))
        {
            List<Location<World>> locationList = new ArrayList<>(event.getLocations());
            for(Location<World> location : locationList)
            {
                BlockState blockState = location.getBlock();
                if(blockState.getType() == BlockTypes.FLOWING_WATER)
                {
                    return;
                }

                if(event.getContext().containsKey(EventContextKeys.OWNER)
                        && event.getContext().get(EventContextKeys.OWNER).isPresent()
                        && event.getContext().get(EventContextKeys.OWNER).get() instanceof Player)
                {

                    Player player = (Player) event.getContext().get(EventContextKeys.OWNER).get();
                    World world = player.getWorld();

                    if(!super.getPlugin().getProtectionManager().canBreak(location, world, player))
                        event.setCancelled(true);
                }
                else
                {
                    if(blockState.getType() == BlockTypes.FLOWING_WATER)
                    {
                        return;
                    }

                    if(!super.getPlugin().getProtectionManager().canBreak(location, location.getExtent()))
                        event.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onBlockBreak(ChangeBlockEvent.Break event)
    {
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
            for(Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                if(super.getPlugin().getProtectionManager().isBlockWhitelistedForPlaceDestroy(transaction.getOriginal().getState().getType()))
                    return;

                if(!super.getPlugin().getProtectionManager().canBreak(transaction.getFinal().getLocation().get(), transaction.getFinal().getLocation().get().getExtent(), (Player) user))
                    event.setCancelled(true);
            }
        }
        else
        {
            if(event.getContext().get(EventContextKeys.SPAWN_TYPE).isPresent())
                return;

            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                if(transaction.getOriginal().getState().getType() == BlockTypes.FLOWING_WATER)
                {
                    return;
                }

                if(!super.getPlugin().getProtectionManager().canBreak(transaction.getFinal().getLocation().get(), transaction.getFinal().getLocation().get().getExtent()))
                    event.setCancelled(true);
            }
        }
    }
}
