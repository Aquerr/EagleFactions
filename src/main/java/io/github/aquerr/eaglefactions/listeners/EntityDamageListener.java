package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class EntityDamageListener
{
    @Listener
    public void onEntityDamage(DamageEntityEvent event)
    {
        EagleFactions.getEagleFactions().getLogger().info("event.getcause().root()");
        EagleFactions.getEagleFactions().getLogger().info(event.getCause().root().toString());

        EntityDamageSource source = (EntityDamageSource)event.getCause().root();

        if(source.getSource() instanceof Player)
        {
            Player player = (Player) source.getSource();

            player.sendMessage(Text.of("YOU ATTACKED SOMEONE!"));

            EagleFactions.getEagleFactions().getLogger().info("Player attacked someone!");
            return;
        }
        return;
    }
}
