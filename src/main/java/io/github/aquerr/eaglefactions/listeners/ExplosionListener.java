package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
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
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExplosionListener extends AbstractListener
{
    public ExplosionListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosionPre(ExplosionEvent.Pre event)
    {
        World world = event.getTargetWorld();
        EventContext eventContext = event.getContext();
        Cause cause = event.getCause();

        //TODO: Improve this code... as it should not happen every time.
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(event.getTargetWorld().getUniqueId(), event.getExplosion().getLocation().getChunkPosition());
        if(optionalChunkFaction.isPresent())
            event.setCancelled(true);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosion(ExplosionEvent.Detonate event)
    {
        List<Location<World>> locationList = new ArrayList<>(event.getAffectedLocations());
        List<Entity> entityList = new ArrayList<>(event.getEntities());
        User user = null;
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        if (cause.root() instanceof TileEntity) {
            user = context.get(EventContextKeys.OWNER)
                    .orElse(context.get(EventContextKeys.NOTIFIER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        } else {
            user = context.get(EventContextKeys.NOTIFIER)
                    .orElse(context.get(EventContextKeys.OWNER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        }

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
//            Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(entityLocation.getExtent().getUniqueId(), entityLocation.getChunkPosition());
            if(user != null)
            {
                if(!super.getPlugin().getProtectionManager().canBreak(entityLocation, entityLocation.getExtent(), user))
                {
                    event.getEntities().remove(entity);
                }
            }
            else if(!super.getPlugin().getProtectionManager().canBreak(entityLocation, entityLocation.getExtent()))
            {
                event.getEntities().remove(entity);
            }
        }

//        for(int i = 0; i < entityList.size(); i++)
//        {
//            Location<World> entityLocation = entityList.get(i).getLocation();
//            if(user != null)
//            {
//                if(!super.getPlugin().getProtectionManager().canBreak(entityLocation, entityLocation.getExtent(), user))
//                {
//                    event.getEntities().remove(i);
//                    i--;
//                }
//            }
//            else if(!super.getPlugin().getProtectionManager().canBreak(entityLocation, entityLocation.getExtent()))
//            {
//                event.getEntities().remove(i);
//                i--;
//            }
//        }
//
        for(Location<World> location : locationList)
        {
//            Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());

            if(user != null)
            {
                if(!super.getPlugin().getProtectionManager().canBreak(location, location.getExtent(), user))
                {
                    event.getAffectedLocations().remove(location);
                }
            }
//            else if(optionalFaction.isPresent() && optionalFaction.get().getName().equalsIgnoreCase("SafeZone"))
//            {
//                event.getAffectedLocations().remove(location);
//            }
            else if(!super.getPlugin().getProtectionManager().canBreak(location, location.getExtent()))
            {
                event.getAffectedLocations().remove(location);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosionPost(ExplosionEvent.Post event)
    {
        final World world = event.getExplosion().getWorld();
        User user = null;
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        // Always use owner for ticking TE's
        // See issue MinecraftPortCentral/GriefPrevention#610 for more information
        if (cause.root() instanceof TileEntity) {
            user = context.get(EventContextKeys.OWNER)
                    .orElse(context.get(EventContextKeys.NOTIFIER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        } else {
            user = context.get(EventContextKeys.NOTIFIER)
                    .orElse(context.get(EventContextKeys.OWNER)
                            .orElse(context.get(EventContextKeys.CREATOR)
                                    .orElse(null)));
        }

        if (user == null) {
            // Check igniter
            final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
            if (living instanceof User) {
                user = (User) living;
            }
        }

        for(Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            BlockSnapshot blockSnapshot = transaction.getOriginal();
            Location<World> location = blockSnapshot.getLocation().orElse(null);
            if(location == null)
                continue;

            Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
            if(!optionalChunkFaction.isPresent())
                continue;

            if(user != null && !super.getPlugin().getProtectionManager().canBreak(location, world, user))
            {
                event.setCancelled(true);
                return;
            }
            else if(user == null && !super.getPlugin().getProtectionManager().canBreak(location, world))
            {
                event.setCancelled(true);
                return;
            }
        }
    }
}
