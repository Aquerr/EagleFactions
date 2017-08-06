package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;

public class EntityDamageListener
{
    @Listener
    public void onEntityDamage(DamageEntityEvent event)
    {
        if(event.getCause().root() instanceof Player)
        {
            EagleFactions.getEagleFactions().getLogger().info("Player attacked someone!");
            return;
        }
    }
}
