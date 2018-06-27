package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ai.SetAITargetEvent;

import java.util.Optional;

@Singleton
public class MobTargetListener extends GenericListener
{
    @Inject
    MobTargetListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions)
    {
        super(cache, settings, eagleFactions);
    }

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
