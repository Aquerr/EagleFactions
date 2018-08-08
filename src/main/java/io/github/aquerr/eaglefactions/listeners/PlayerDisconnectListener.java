package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerDisconnectListener extends AbstractListener
{
    public PlayerDisconnectListener(EagleFactions plugin)
    {
        super(plugin);
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
