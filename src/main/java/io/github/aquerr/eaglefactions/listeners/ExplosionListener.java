package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExplosionListener extends AbstractListener
{
    public ExplosionListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onExplosionPre(ExplosionEvent.Pre event)
    {
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(event.getTargetWorld().getUniqueId(), event.getExplosion().getLocation().getChunkPosition());
        if(optionalChunkFaction.isPresent())
            event.setCancelled(true);
    }

    @Listener(order = Order.EARLY)
    public void onExplosion(ExplosionEvent.Detonate event)
    {
        List<Location<World>> locationList = new ArrayList<>(event.getAffectedLocations());
        for(Location<World> location : locationList)
        {
            Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(event.getExplosion().getWorld().getUniqueId(), location.getChunkPosition());
            if(optionalChunkFaction.isPresent())
            {
                event.getAffectedLocations().remove(location);
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onExplosionPost(ExplosionEvent.Post event)
    {
        final World world = event.getExplosion().getWorld();
        User user = null;
        final Cause cause = event.getCause();
        final EventContext context = event.getContext();
        if (user == null) {
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

            if(!super.getPlugin().getProtectionManager().canBreak(location, world, user))
            {
                event.setCancelled(true);
                return;
            }
        }
    }
}
