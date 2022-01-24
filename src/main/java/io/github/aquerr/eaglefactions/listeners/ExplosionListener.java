package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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

        User user = null;
//        if (cause.root() instanceof BlockEntity) {
//            user = context.get(EventContextKeys.CREATOR)
//                    .orElse(context.get(EventContextKeys.PLAYER)
//                                    .orElse(null)));
//        } else {
//            user = context.get(EventContextKeys.PLAYER)
//                    .orElse(context.get(EventContextKeys.AUDIENCE)
//                                    .orElse(null)));
//        }

        if(event.cause().containsType(ServerPlayer.class))
        {
            user = event.cause().first(ServerPlayer.class).get().user();
        }
        else if(event.cause().containsType(User.class))
        {
            user = event.cause().first(User.class).get();
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
            if (!super.getPlugin().getProtectionManager().canExplode(location, user, false).hasAccess())
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
        User user = null;
        final Cause cause = event.cause();
        final EventContext context = event.context();
//        if (cause.root() instanceof TileEntity) {
//            user = context.get(EventContextKeys.OWNER)
//                    .orElse(context.get(EventContextKeys.NOTIFIER)
//                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null)));
//        } else {
//            user = context.get(EventContextKeys.NOTIFIER)
//                    .orElse(context.get(EventContextKeys.OWNER)
//                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null)));
//        }

        if(event.cause().containsType(ServerPlayer.class))
        {
            user = event.cause().first(ServerPlayer.class).get().user();
        }
        else if(event.cause().containsType(User.class))
        {
            user = event.cause().first(User.class).get();
        }

        event.filterEntities(new FilterEntities(this.getPlugin().getProtectionManager(), user));
        event.filterAffectedLocations(new FilterLocations(this.getPlugin().getProtectionManager(), user));
    }

//    @Listener(order = Order.FIRST, beforeModifications = true)
//    public void onExplosionPost(final ExplosionEvent.Post event)
//    {
//        User user = null;
//        final Cause cause = event.getCause();
//        final EventContext context = event.getContext();
//        // Thanks to GriefPrevention
//        // Always use owner for ticking TE's
//        if (cause.root() instanceof TileEntity) {
//            user = context.get(EventContextKeys.OWNER)
//                    .orElse(context.get(EventContextKeys.NOTIFIER)
//                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null)));
//        } else {
//            user = context.get(EventContextKeys.NOTIFIER)
//                    .orElse(context.get(EventContextKeys.OWNER)
//                            .orElse(context.get(EventContextKeys.CREATOR)
//                                    .orElse(null)));
//        }
//
//        if (user == null) {
//            // Check igniter
//            final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
//            if (living instanceof User) {
//                user = (User) living;
//            }
//        }
//
//        for(Transaction<BlockSnapshot> transaction : event.getTransactions())
//        {
//            BlockSnapshot blockSnapshot = transaction.getOriginal();
//            Location<World> location = blockSnapshot.getLocation().orElse(null);
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

    private static class FilterEntities implements Predicate<Entity>
    {
        private final ProtectionManager protectionManager;
        private final User user;

        public FilterEntities(ProtectionManager protectionManager, final User user)
        {
            this.protectionManager = protectionManager;
            this.user = user;
        }

        @Override
        public boolean test(Entity entity)
        {
            final ServerLocation entityLocation = entity.serverLocation();
            if(user != null)
            {
                if(!protectionManager.canExplode(entityLocation, user, false).hasAccess())
                {
                    return false;
                }
            }
            else if(!protectionManager.canExplode(entityLocation).hasAccess())
            {
                return false;
            }
            return true;
        }
    }

    private static class FilterLocations implements Predicate<ServerLocation>
    {
        private final ProtectionManager protectionManager;
        private final User user;

        public FilterLocations(ProtectionManager protectionManager, final User user)
        {
            this.protectionManager = protectionManager;
            this.user = user;
        }

        @Override
        public boolean test(ServerLocation location)
        {
            if(user != null)
            {
                if(!protectionManager.canExplode(location, user, false).hasAccess())
                {
                    return false;
                }
            }
            else if(!protectionManager.canExplode(location).hasAccess())
            {
                return false;
            }
            return true;
        }
    }
}
