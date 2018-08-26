package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
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
}
