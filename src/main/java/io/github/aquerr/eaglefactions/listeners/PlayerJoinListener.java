package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PlayerJoinListener extends AbstractListener
{
    public PlayerJoinListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(ClientConnectionEvent.Join event)
    {
        if(event.getCause().root() instanceof Player)
        {
            Player player = (Player) event.getCause().root();

            if (player.hasPermission(PluginPermissions.VERSION_NOTIFY) && !VersionChecker.isLatest(PluginInfo.VERSION))
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.A_NEW_VERSION_OF + " ", TextColors.AQUA, "Eagle Factions", TextColors.GREEN, " " + PluginMessages.IS_AVAILABLE));
            }

            if(!getPlugin().getPlayerManager().checkIfPlayerExists(player.getUniqueId(), player.getName()))
            {
                //Create player file and set power.
                getPlugin().getPlayerManager().addPlayer(player.getUniqueId(), player.getName());
            }

            getPlugin().getPlayerManager().updatePlayerName(player.getUniqueId(), player.getName());
            getPlugin().getPowerManager().startIncreasingPower(player.getUniqueId());

            //Check if the world that player is connecting to is already in the config file
            if (!getPlugin().getConfiguration().getConfigFields().getDetectedWorldNames().contains(player.getWorld().getName()))
            {
                getPlugin().getConfiguration().getConfigFields().addWorld(player.getWorld().getName());
            }
        }
    }
}
