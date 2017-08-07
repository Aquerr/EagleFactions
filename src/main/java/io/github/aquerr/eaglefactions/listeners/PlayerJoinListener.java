package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

public class PlayerJoinListener
{
    @Listener
    public void onPlayerJoin(ClientConnectionEvent event)
    {

        if(event.getCause().root() instanceof Player)
        {
            Player player = (Player) event.getCause().root();

            if(PowerService.checkIfPlayerExists(player.getUniqueId()))
            {
                return;
            }
            else
            {
                //Create player file and set power.
                PowerService.addPlayer(player.getUniqueId());
            }

        }
    }
}
