package io.github.aquerr.eaglefactions.listeners.faction;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.events.FactionCreateEvent;
import io.github.aquerr.eaglefactions.listeners.AbstractListener;
import io.github.aquerr.eaglefactions.tab.FactionTabListService;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;

public class FactionCreateListener extends AbstractListener
{
    private final FactionTabListService factionTabListService;

    public FactionCreateListener(EagleFactions plugin, FactionTabListService factionTabListService)
    {
        super(plugin);
        this.factionTabListService = factionTabListService;
    }

    @Listener(order = Order.POST)
    @IsCancelled(value = Tristate.FALSE)
    public void onFactionCreate(final FactionCreateEvent.Post event)
    {
        if (event.getCreator() != null)
        {
            factionTabListService.updateTabListForPlayer((ServerPlayer) event.getCreator());
        }
    }
}
