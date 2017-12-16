package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.services.PlayerService;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

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
                PowerService.increasePower(player.getUniqueId());
                return;
            }
            else
            {
                //Create player file and set power.
                PowerService.addPlayer(player.getUniqueId());
                PlayerService.setPlayerChunkPosition(player.getUniqueId(), player.getLocation().getChunkPosition());
                return;
            }

        }
    }
}
