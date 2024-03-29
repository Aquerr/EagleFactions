package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.scheduling.PowerIncrementTask;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.time.Instant;
import java.util.Optional;

public class PlayerDisconnectListener extends AbstractListener
{
    public PlayerDisconnectListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    public void onDisconnect(ServerSideConnectionEvent.Disconnect event, @Root ServerPlayer player)
    {
        if (super.getPlugin().getPVPLogger().isActive() && EagleFactionsPlugin.getPlugin().getPVPLogger().isPlayerBlocked(player))
        {
            player.damage(1000, DamageSource.builder().type(DamageTypes.ATTACK).build());
            super.getPlugin().getPVPLogger().removePlayer(player);
        }

        EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.remove(player.uniqueId());

        final Optional<Faction> optionalFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        optionalFaction.ifPresent(faction -> getPlugin().getFactionLogic().setLastOnline(faction, Instant.now()));

        FactionsCache.removePlayer(player.uniqueId());

        EagleFactionsScheduler.getInstance().cancelTask(PowerIncrementTask.TASK_NAME_GENERATOR.apply(player.uniqueId()));
    }
}
