package io.github.aquerr.eaglefactions.listeners.faction;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.FactionKickEvent;
import io.github.aquerr.eaglefactions.listeners.AbstractListener;
import io.github.aquerr.eaglefactions.scheduling.TabListUpdater;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

import java.util.List;

public class FactionKickListener extends AbstractListener
{
    public FactionKickListener(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    @IsCancelled(value = Tristate.FALSE)
    public void onPlayerFactionKick(final FactionKickEvent.Post event)
    {
        final Faction faction = event.getFaction();
        final FactionPlayer kickedPlayer = event.getKickedPlayer();

        final List<ServerPlayer> onlineFactionPlayers = super.getPlugin().getFactionLogic().getOnlinePlayers(faction);
        for(final Player player : onlineFactionPlayers)
        {
            if (player.equals(event.getCreator()))
                continue;

            if(player.name().equals(kickedPlayer.getName()))
                continue;

            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text("Player " + kickedPlayer.getName() + " has been kicked from the faction.")));
        }
        TabListUpdater.requestUpdate();
    }
}
