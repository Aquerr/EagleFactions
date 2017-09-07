package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

public class EntitySpawnListener
{
    @Listener
    public void onEntitySpawn(SpawnEntityEvent event)
    {

        for (Entity entity: event.getEntities())
        {
            //EagleFactions.getEagleFactions().getLogger().info(entity.toString());

            if(entity.toString().contains("EntityCustomNpc")) return;

            if(entity instanceof Hostile)
            {
                if((MainLogic.getMobSpawning() == false) && FactionLogic.isClaimed(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition()))
                {
                  //  EagleFactions.getEagleFactions().getLogger().info("Entity is a Hostile!!!! :O");
                  //  EagleFactions.getEagleFactions().getLogger().info("Printing enity: " + entity.toString());
                  //  EagleFactions.getEagleFactions().getLogger().info("Prining spawn cause: " + event.getCause().toString());

                    event.setCancelled(true);
                    return;
                }
            }
            else
            {
               // if(FactionLogic.getFactionNameByChunk(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition()).equals("SafeZone"))
               // {
               //     EagleFactions.getEagleFactions().getLogger().info("Entity is friendly. :D");
               //     EagleFactions.getEagleFactions().getLogger().info("Printing enity: " + entity.toString());
               //     EagleFactions.getEagleFactions().getLogger().info("Prining spawn cause: " + event.getCause().toString());
               // }

                return;
            }

            //EntityType for CustomNPC (it isn't count as a monster) => EntityCustomNpc


        }
    }
}
