package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerDeathListener extends AbstractListener
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;
    private final PowerConfig powerConfig;
    private final MessageService messageService;

    public PlayerDeathListener(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.powerConfig = plugin.getConfiguration().getPowerConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.messageService = plugin.getMessageService();
    }

    @Listener(order = Order.POST)
    public void onPlayerDeath(final DestructEntityEvent.Death event, final @Getter("entity") ServerPlayer player)
    {
        CompletableFuture.runAsync(() -> super.getPlugin().getPowerManager().decreasePower(player.uniqueId()))
                .thenRun(() -> player.sendMessage(messageService.resolveComponentWithMessage("power.decreased-by", this.powerConfig.getPowerDecrement())
                                .append(Component.newline())
                                .append(messageService.resolveComponentWithMessage("power.current-power",
                                        super.getPlugin().getPowerManager().getPlayerPower(player.uniqueId()),
                                        super.getPlugin().getPowerManager().getPlayerMaxPower(player.uniqueId())))));

        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition());
        if (this.protectionConfig.getWarZoneWorldNames().contains(player.world().key().asString()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().isWarZone()))
        {
            super.getPlugin().getPlayerManager().setDeathInWarZone(player.uniqueId(), true);
        }

        if (this.factionsConfig.shouldBlockHomeAfterDeathInOwnFaction())
        {
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
            if (optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent() && optionalChunkFaction.get().getName().equals(optionalPlayerFaction.get().getName()))
            {
                super.getPlugin().getAttackLogic().blockHome(player.uniqueId());
            }
        }

        if(super.getPlugin().getPVPLogger().isActive() && super.getPlugin().getPVPLogger().isPlayerBlocked(player))
        {
            super.getPlugin().getPVPLogger().removePlayer(player);
        }
    }
}
