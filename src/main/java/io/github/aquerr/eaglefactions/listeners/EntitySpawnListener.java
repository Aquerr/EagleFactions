package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class EntitySpawnListener
{
    @Listener
    public void onEntitySpawn(SpawnEntityEvent event)
    {
        for (Entity entity: event.getEntities())
        {
            //EntityType for CustomNPC (it isn't count as a monster) => EntityCustomNpc
            if(entity.toString().contains("EntityCustomNpc")) return;

            if(entity instanceof Hostile)
            {
                if((MainLogic.getMobSpawning() == false) && FactionLogic.isClaimed(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition()))
                {
                    event.setCancelled(true);
                    return;
                }
            }
            else if(entity instanceof Player)
            {
                if(MainLogic.shouldSpawnAtHomeAfterDeath())
                {
                    Player player = (Player)entity;

                    if(FactionLogic.getFactionName(player.getUniqueId()) != null)
                    {
                        event.setCancelled(true);
                        player.setLocation(new Location<World>(event.getTargetWorld(), FactionLogic.getHome(FactionLogic.getFactionName(player.getUniqueId()))));
                        return;
                    }
                }
            }
        }
    }
}
