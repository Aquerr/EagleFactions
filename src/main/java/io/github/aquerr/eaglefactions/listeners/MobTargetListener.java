package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ai.SetAITargetEvent;

import java.util.Optional;

public class MobTargetListener
{
    @Listener
    public void onTargetChange(SetAITargetEvent event)
    {
        if (event.getTarget().isPresent() && event.getTarget().get() instanceof Player)
        {
            Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(event.getTarget().get().getWorld().getUniqueId(), ((Player) event.getTarget().get()).getLocation().getChunkPosition());
            if (optionalChunkFaction.isPresent() && optionalChunkFaction.get().Name.equals("SafeZone"))
            {
                event.setCancelled(true);
                return;
            }
        }
    }
}
