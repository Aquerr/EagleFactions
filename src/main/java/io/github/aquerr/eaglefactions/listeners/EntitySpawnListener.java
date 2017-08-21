package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.World;

public class EntitySpawnListener
{
    @Listener
    public void onEntitySpawn(SpawnEntityEvent event)
    {
        for (Entity entity: event.getEntities())
        {
            if(entity instanceof Player)
            {

            }
            else
            {
                if(MainLogic.getMobSpawning() == false && FactionLogic.isClaimed(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition()))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
