package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.services.PlayerService;
import io.github.aquerr.eaglefactions.services.PowerService;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PlayerJoinListener
{
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event)
    {

        if(event.getCause().root() instanceof Player)
        {
            Player player = (Player) event.getCause().root();

            if (player.hasPermission(PluginPermissions.VersionNotify) && !VersionChecker.isLatest(PluginInfo.Version))
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "A new version of ", TextColors.AQUA, "Eagle Factions", TextColors.GREEN, " is available!"));
            }

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
