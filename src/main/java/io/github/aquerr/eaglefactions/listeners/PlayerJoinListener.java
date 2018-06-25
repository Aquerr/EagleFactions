package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Singleton
public class PlayerJoinListener extends GenericListener
{

    @Inject
    PlayerJoinListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions)
    {
        super(cache, settings, eagleFactions);
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event)
    {

        if (event.getCause().root() instanceof Player)
        {
            Player player = (Player) event.getCause().root();

            if (player.hasPermission(PluginPermissions.VersionNotify) && !VersionChecker.isLatest(PluginInfo.Version))
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.A_NEW_VERSION_OF + " ", TextColors.AQUA, "Eagle Factions", TextColors.GREEN, " " + PluginMessages.IS_AVAILABLE));
            }

            if (PowerManager.checkIfPlayerExists(player.getUniqueId()))
            {
                PowerManager.startIncreasingPower(player.getUniqueId());
            } else
            {
                //Create player file and set power.
                PowerManager.addPlayer(player.getUniqueId());
            }

            //Check if the world that player is connecting to is already in the config file
            if (!settings.getDetectedWorldNames().contains(player.getWorld().getName()))
            {
                settings.addWorld(player.getWorld().getName());
            }
        }
    }
}
