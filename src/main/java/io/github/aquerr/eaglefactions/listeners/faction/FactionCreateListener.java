package io.github.aquerr.eaglefactions.listeners.faction;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.events.FactionCreateEvent;
import io.github.aquerr.eaglefactions.listeners.AbstractListener;
import io.github.aquerr.eaglefactions.scheduling.TabListUpdater;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

public class FactionCreateListener extends AbstractListener
{
    public FactionCreateListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    @IsCancelled(value = Tristate.FALSE)
    public void onFactionCreate(final FactionCreateEvent.Post event)
    {
        new TabListUpdater(getPlugin().getConfiguration(), getPlugin().getPlayerManager()).updateTabListForPlayer((ServerPlayer) event.getCreator());
    }
}
