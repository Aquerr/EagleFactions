package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class ExplosionListener extends AbstractListener
{
    public ExplosionListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosionPre(ExplosionEvent.Pre event)
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

        Location<World> location = event.getExplosion().getLocation();
        if (user == null)
        {
            if(!super.getPlugin().getProtectionManager().canExplode(location))
            {
                event.setCancelled(true);
                return;
            }
        }
        else
        {
            if (!super.getPlugin().getProtectionManager().canExplode(location, user))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosion(ExplosionEvent.Detonate event)
    {
        List<Location<World>> locationList = new ArrayList<>(event.getAffectedLocations());
        List<Entity> entityList = new ArrayList<>(event.getEntities());
        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
        }

        for(Entity entity : entityList)
        {
            Location<World> entityLocation = entity.getLocation();
            if(user != null)
            {
                if(!super.getPlugin().getProtectionManager().canExplode(entityLocation, user))
                {
                    event.getEntities().remove(entity);
                }
            }
            else if(!super.getPlugin().getProtectionManager().canExplode(entityLocation))
            {
                event.getEntities().remove(entity);
            }
        }

        for(Location<World> location : locationList)
        {
            if(user != null)
            {
                if(!super.getPlugin().getProtectionManager().canExplode(location, user))
                {
                    event.getAffectedLocations().remove(location);
                }
            }
            else if(!super.getPlugin().getProtectionManager().canExplode(location))
            {
                event.getAffectedLocations().remove(location);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosionPost(ExplosionEvent.Post event)
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

        for(Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            BlockSnapshot blockSnapshot = transaction.getOriginal();
            Location<World> location = blockSnapshot.getLocation().orElse(null);
            if(location == null)
                continue;

            if (user != null)
            {
                if (!super.getPlugin().getProtectionManager().canExplode(location, user))
                {
                    event.setCancelled(true);
                    return;
                }
            }
            else
            {
                if (!super.getPlugin().getProtectionManager().canExplode(location))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
