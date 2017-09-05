package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.monster.Monster;
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
            EagleFactions.getEagleFactions().getLogger().info(entity.toString());

            if(entity instanceof Monster)
            {
                EagleFactions.getEagleFactions().getLogger().info("Entity is a monster!!!! :O");
                EagleFactions.getEagleFactions().getLogger().info("Printing enity type: " + entity.getType().toString());
                EagleFactions.getEagleFactions().getLogger().info("Prining spawn couse: " + event.getCause().toString());



                if(MainLogic.getMobSpawning() == false && FactionLogic.isClaimed(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition()))
                {
                    event.setCancelled(true);
                    return;
                }
            }
            else
            {
                EagleFactions.getEagleFactions().getLogger().info("Entity is not a monster. :D");
                EagleFactions.getEagleFactions().getLogger().info("Printing enity type: " + entity.getType().toString());
                EagleFactions.getEagleFactions().getLogger().info("Prining spawn couse: " + event.getCause().toString());
            }

            //EntityType for CustomNPC (it counts as a monster) => EntityCustomNpc


        }
    }
}
