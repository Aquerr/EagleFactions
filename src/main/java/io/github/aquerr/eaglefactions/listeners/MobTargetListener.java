package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ai.SetAITargetEvent;

public class MobTargetListener
{
    @Listener
    public void onTargetChange(SetAITargetEvent event)
    {
        if (event.getTarget().isPresent() && event.getTarget().get() instanceof Player)
        {
            if (FactionLogic.getFactionByChunk(event.getTarget().get().getWorld().getUniqueId(), event.getTarget().get().getLocation().getChunkPosition()).equals("SafeZone"))
            {
                event.setCancelled(true);
                return;
            }
        }
    }
}
