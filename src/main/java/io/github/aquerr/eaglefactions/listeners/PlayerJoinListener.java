package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PlayerJoinListener extends AbstractListener
{
    public PlayerJoinListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event)
    {

        if(event.getCause().root() instanceof Player)
        {
            Player player = (Player) event.getCause().root();

            if (player.hasPermission(PluginPermissions.VersionNotify) && !VersionChecker.isLatest(PluginInfo.Version))
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.A_NEW_VERSION_OF + " ", TextColors.AQUA, "Eagle Factions", TextColors.GREEN, " " + PluginMessages.IS_AVAILABLE));
            }

            if(getPlugin().getPlayerManager().checkIfPlayerExists(player.getUniqueId(), player.getName()))
            {
                getPlugin().getPowerManager().startIncreasingPower(player.getUniqueId());
            }
            else
            {
                //Create player file and set power.
                getPlugin().getPlayerManager().addPlayer(player.getUniqueId(), player.getName());
            }

            //Check if the world that player is connecting to is already in the config file
            if (!getPlugin().getConfiguration().getConfigFileds().getDetectedWorldNames().contains(player.getWorld().getName()))
            {
                getPlugin().getConfiguration().getConfigFileds().addWorld(player.getWorld().getName());
            }
        }
    }
}
