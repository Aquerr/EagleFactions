package io.github.aquerr.eaglefactions.common.listeners.faction;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.listeners.AbstractListener;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;

import java.util.List;

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
        final List<Player> factionPlayers = super.getPlugin().getFactionLogic().getOnlinePlayers(faction);
        for (final Player factionPlayer : factionPlayers)
        {
            if (factionPlayer.getName().equals(player.getName()))
                continue;
            factionPlayer.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, player.getName(), TextColors.AQUA, " left your faction."));
        }
    }
}
