package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class FireBlockPlaceListener
{
//    @Listener
//    public void onIgnition(ChangeBlockEvent.Pre event)
//    {
//        //TODO: Check if this will work for fire from lightning strike.
//
//        if (event.getCause().containsNamed(NamedCause.IGNITER))
//        {
//            event.getLocations().forEach(x ->
//            {
//                if (FactionLogic.getFactionNameByChunk(event.getTargetWorld().getUniqueId(), x.getChunkPosition()).equals("SafeZone") ||
//                        FactionLogic.getFactionNameByChunk(event.getTargetWorld().getUniqueId(), x.getChunkPosition()).equals("WarZone"))
//                {
//                    event.setCancelled(true);
//                    return;
//                }
//            });
//        }
//
//        if (event.getCause().containsNamed(NamedCause.FIRE_SPREAD))
//        {
//            event.getLocations().forEach(x ->
//            {
//                if (FactionLogic.getFactionNameByChunk(event.getTargetWorld().getUniqueId(), x.getChunkPosition()).equals("SafeZone") ||
//                        FactionLogic.getFactionNameByChunk(event.getTargetWorld().getUniqueId(), x.getChunkPosition()).equals("WarZone"))
//                {
//                    event.setCancelled(true);
//                    return;
//                }
//            });
//        }
//    }

    @Listener
    public void onIgnite(ChangeBlockEvent.Place event)
    {
        if (event.getCause().root() instanceof Player)
        {
            Player player = (Player)event.getCause().root();

            if (!EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                event.getTransactions().forEach(x->
                {
                    if (x.getFinal().getState().getType() == BlockTypes.FIRE
                            && (FactionLogic.getFactionNameByChunk(x.getFinal().getWorldUniqueId(), x.getFinal().getLocation().get().getChunkPosition()).equals("SafeZone"))
                            || FactionLogic.getFactionNameByChunk(x.getFinal().getWorldUniqueId(), x.getFinal().getLocation().get().getChunkPosition()).equals("WarZone"))
                    {
                        event.setCancelled(true);
                    }
                });
            }
        }
        else
        {
            event.getTransactions().forEach(x->
            {
                if (x.getFinal().getState().getType() == BlockTypes.FIRE
                        && (FactionLogic.getFactionNameByChunk(x.getFinal().getWorldUniqueId(), x.getFinal().getLocation().get().getChunkPosition()).equals("SafeZone"))
                        || FactionLogic.getFactionNameByChunk(x.getFinal().getWorldUniqueId(), x.getFinal().getLocation().get().getChunkPosition()).equals("WarZone"))
                {
                    event.setCancelled(true);
                }
            });
        }
    }
}