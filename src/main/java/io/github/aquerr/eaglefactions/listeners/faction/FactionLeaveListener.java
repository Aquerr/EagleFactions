package io.github.aquerr.eaglefactions.listeners.faction;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import io.github.aquerr.eaglefactions.listeners.AbstractListener;
import io.github.aquerr.eaglefactions.scheduling.TabListUpdater;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.util.Tristate;

import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class FactionLeaveListener extends AbstractListener
{
    public FactionLeaveListener(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    @IsCancelled(value = Tristate.FALSE)
    public void onFactionLeave(final FactionLeaveEvent.Post event, @Root final Player player)
    {
        //Notify other faction members about someone leaving the faction
        final Faction faction = event.getFaction();
        final List<ServerPlayer> factionPlayers = super.getPlugin().getFactionLogic().getOnlinePlayers(faction);
        for (final Player factionPlayer : factionPlayers)
        {
            if (factionPlayer.name().equals(player.name()))
                continue;
            factionPlayer.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(player.name(), GOLD)).append(Component.text(" left your faction.", AQUA)));
        }
        TabListUpdater.requestUpdate();
    }
}
