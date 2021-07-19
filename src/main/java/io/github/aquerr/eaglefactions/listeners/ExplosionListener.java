package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.ArrayList;
import java.util.List;

public class ExplosionListener extends AbstractListener
{
    public ExplosionListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosionPre(final ExplosionEvent.Pre event)
    {
        final EventContext context = event.context();
        final Cause cause = event.cause();

        ServerPlayer user = null;
//        if (cause.root() instanceof BlockEntity) {
//            user = context.get(EventContextKeys.PLAYER)
//                    .orElse(context.get(EventContextKeys.NOTIFIER)
//                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null);
//        } else {
//            user = context.get(EventContextKeys.PLAYER)
//                    .orElse(context.get(EventContextKeys.OWNER)
//                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null);
//        }

        if(event.cause().containsType(ServerPlayer.class))
        {
            user = event.cause().first(ServerPlayer.class).get();
        }

        final ServerLocation location = event.explosion().serverLocation();
        if (user == null)
        {
            if(!super.getPlugin().getProtectionManager().canExplode(location).hasAccess())
            {
                event.setCancelled(true);
                return;
            }
        }
        else
        {
            if (!super.getPlugin().getProtectionManager().canExplode(location, user.user(), false).hasAccess())
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosion(final ExplosionEvent.Detonate event)
    {
        final List<ServerLocation> locationList = new ArrayList<>(event.affectedLocations());
        final List<Entity> entityList = new ArrayList<>(event.entities());
        ServerPlayer user = null;
        final Cause cause = event.cause();
        final EventContext context = event.context();
//        if (cause.root() instanceof BlockEntity) {
//            user = context.get(EventContextKeys.PLAYER)
//                    .orElse(context.get(EventContextKeys.NOTIFIER)
//                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null);
//        } else {
//            user = context.get(EventContextKeys.PLAYER)
//                    .orElse(context.get(EventContextKeys.OWNER)
//                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null);
//        }

        if(event.cause().containsType(ServerPlayer.class))
        {
            user = event.cause().first(ServerPlayer.class).get();
        }
//        else if(event.cause().containsType(Player.class))
//        {
//            user = event.cause().first(Player.class).get();
//        }

        for(final Entity entity : entityList)
        {
            final ServerLocation entityLocation = entity.serverLocation();
            if(user != null)
            {
                if(!super.getPlugin().getProtectionManager().canExplode(entityLocation, user.user(), false).hasAccess())
                {
                    event.entities().remove(entity);
                }
            }
            else if(!super.getPlugin().getProtectionManager().canExplode(entityLocation).hasAccess())
            {
                event.entities().remove(entity);
            }
        }

        for(final ServerLocation location : locationList)
        {
            if(user != null)
            {
                if(!super.getPlugin().getProtectionManager().canExplode(location, user.user(), false).hasAccess())
                {
                    event.affectedLocations().remove(location);
                }
            }
            else if(!super.getPlugin().getProtectionManager().canExplode(location).hasAccess())
            {
                event.affectedLocations().remove(location);
            }
        }
    }

//    @Listener(order = Order.FIRST, beforeModifications = true)
//    public void onExplosionPost(final ExplosionEvent.Post event)
//    {
//        Player user = null;
//        final Cause cause = event.cause();
//        final EventContext context = event.context();
//        // Thanks to GriefPrevention
//        // Always use owner for ticking TE's
//        if (cause.root() instanceof BlockEntity) {
//            user = context.get(EventContextKeys.PLAYER)
////                    .orElse(context.get(EventContextKeys.NOTIFIER)
////                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null);
//        } else {
//            user = context.get(EventContextKeys.PLAYER)
////                    .orElse(context.get(EventContextKeys.OWNER)
////                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null);
//        }
//
//        if (user == null) {
//            // Check igniter
//            final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
//            if (living instanceof Player) {
//                user = (Player) living;
//            }
//        }
//
//        for(Transaction<BlockSnapshot> transaction : event.getTransactions())
//        {
//            BlockSnapshot blockSnapshot = transaction.original();
//            Location<World> location = blockSnapshot.location().orElse(null);
//            if(location == null)
//                continue;
//
//            if (user != null)
//            {
//                if (!super.getPlugin().getProtectionManager().canExplode(location, user, false).hasAccess())
//                {
//                    event.setCancelled(true);
//                    return;
//                }
//            }
//            else
//            {
//                if (!super.getPlugin().getProtectionManager().canExplode(location).hasAccess())
//                {
//                    event.setCancelled(true);
//                    return;
//                }
//            }
//        }
//    }
}
