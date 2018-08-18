package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
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

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event, @Root Player player)
    {
        if (super.getPlugin().getPVPLogger().isActive() && EagleFactions.getPlugin().getPVPLogger().isPlayerBlocked(player))
        {
            player.damage(1000, DamageSource.builder().type(DamageTypes.ATTACK).build());
            super.getPlugin().getPVPLogger().removePlayer(player);
        }

        Optional<Faction> optionalFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        optionalFaction.ifPresent(faction -> getPlugin().getFactionLogic().setLastOnline(faction, Instant.now()));
    }
}
