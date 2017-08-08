package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.Text;

public class PlayerDeathListener
{
    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event)
    {
        EagleFactions.getEagleFactions().getLogger().info(event.getCause().root().toString());

        if(event.getTargetEntity() instanceof Player)
        {
            Player player = (Player)event.getTargetEntity();

            player.sendMessage(Text.of("And now... you are dead. :/"));

            EagleFactions.getEagleFactions().getLogger().info("DEATHHHHHHHHHHHHHHHHHHHHHH");
        }
    }
}
