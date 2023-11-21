package io.github.aquerr.eaglefactions.listeners.faction;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.events.FactionTagColorUpdateEvent;
import io.github.aquerr.eaglefactions.listeners.AbstractListener;
import io.github.aquerr.eaglefactions.scheduling.TabListUpdater;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

public class FactionTagColorUpdateListener extends AbstractListener
{
    public FactionTagColorUpdateListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    @IsCancelled(value = Tristate.FALSE)
    public void onTagColorUpdate(final FactionTagColorUpdateEvent.Post event)
    {
        TabListUpdater.requestUpdate();
    }
}
