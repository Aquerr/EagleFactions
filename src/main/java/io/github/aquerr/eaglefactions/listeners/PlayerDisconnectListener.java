package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

@Singleton
public class PlayerDisconnectListener extends GenericListener
{
    @Inject
    PlayerDisconnectListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions)
    {
        super(cache, settings, eagleFactions);
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player)
    {
        if (EagleFactions.getPlugin().getPVPLogger().isActive() && EagleFactions.getPlugin().getPVPLogger().isPlayerBlocked(player))
        {
            player.damage(1000, DamageSource.builder().type(DamageTypes.ATTACK).build());
            EagleFactions.getPlugin().getPVPLogger().removePlayer(player);
        }
    }
}
