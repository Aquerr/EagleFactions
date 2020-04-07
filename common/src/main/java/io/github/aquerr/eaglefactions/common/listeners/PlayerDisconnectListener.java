package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.time.Instant;
import java.util.Optional;

public class PlayerDisconnectListener extends AbstractListener
{
    public PlayerDisconnectListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    public void onDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player)
    {
        if (super.getPlugin().getPVPLogger().isActive() && EagleFactionsPlugin.getPlugin().getPVPLogger().isPlayerBlocked(player))
        {
            player.damage(1000, DamageSource.builder().type(DamageTypes.ATTACK).build());
            super.getPlugin().getPVPLogger().removePlayer(player);
        }

        final Optional<Faction> optionalFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        optionalFaction.ifPresent(faction -> getPlugin().getFactionLogic().setLastOnline(faction, Instant.now()));

        //TODO: Unload player cache...
        FactionsCache.removePlayer(player.getUniqueId());
    }
}
