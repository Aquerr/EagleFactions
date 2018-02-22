package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.logic.PVPLogger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerDisconnectListener
{
    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player)
    {
        if (PVPLogger.wasAttacked(player))
        {
            player.damage(1000, DamageSource.builder().type(DamageTypes.ATTACK).build());
            PVPLogger.removePlayer(player);
        }
    }
}
