package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;

public class FireSpreadListener
{
    @Listener
    public void onFireSpread(ChangeBlockEvent.Pre event)
    {
        //TODO: Check if this will work for fire from lightning strike.
        if (event.getCause().containsNamed(NamedCause.FIRE_SPREAD))
        {
            event.getLocations().forEach(x ->
            {
                if (FactionLogic.getFactionNameByChunk(event.getTargetWorld().getUniqueId(), x.getChunkPosition()).equals("SafeZone"))
                {
                    event.setCancelled(true);
                    return;
                }
            });
        }
    }

//    @Listener
//    public void onIgnite(ChangeBlockEvent.Place event)
//    {
//        EagleFactions.getEagleFactions().getLogger().info("Get Transactions: " + event.getTransactions());
//        //EagleFactions.getEagleFactions().getLogger().info("Break Event Cause: " + event.getCause().toString());
//    }
}