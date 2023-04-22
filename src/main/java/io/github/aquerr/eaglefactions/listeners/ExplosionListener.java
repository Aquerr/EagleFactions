package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.UUID;
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
        UUID possibleUserUUID = event.context().get(EventContextKeys.CREATOR).orElse(null);
        final ServerLocation location = event.explosion().serverLocation();
        if (possibleUserUUID == null)
        {
            if(!super.getPlugin().getProtectionManager().canExplode(location).hasAccess())
            {
                event.setCancelled(true);
            }
        }
        else
        {
            User user = getPlugin().getPlayerManager().getPlayer(possibleUserUUID)
                    .map(ServerPlayer::user)
                    .orElse(null);
            if (user != null && !super.getPlugin().getProtectionManager().canExplode(location, user, false).hasAccess())
            {
                event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onExplosion(final ExplosionEvent.Detonate event)
    {
        UUID possibleUserUUID = event.context().get(EventContextKeys.CREATOR).orElse(null);
        if (possibleUserUUID == null)
            return;
        User user = getPlugin().getPlayerManager().getPlayer(possibleUserUUID)
                .map(ServerPlayer::user)
                .orElse(null);
        event.filterEntities(new FilterEntities(this.getPlugin().getProtectionManager(), user));
        event.filterAffectedLocations(new FilterLocations(this.getPlugin().getProtectionManager(), user));
    }

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
                return protectionManager.canExplode(entityLocation, user, false).hasAccess();
            }
            else return protectionManager.canExplode(entityLocation).hasAccess();
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
                return protectionManager.canExplode(location, user, false).hasAccess();
            }
            else return protectionManager.canExplode(location).hasAccess();
        }
    }
}
