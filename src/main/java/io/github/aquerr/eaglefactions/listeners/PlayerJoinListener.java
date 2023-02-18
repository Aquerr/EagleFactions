package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener extends AbstractListener
{
    private final ProtectionConfig protectionConfig;
    private final MessageService messageService;

    public PlayerJoinListener(final EagleFactions plugin)
    {
        super(plugin);
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.messageService = plugin.getMessageService();
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(final ServerSideConnectionEvent.Join event, final @First ServerPlayer player)
    {
        CompletableFuture.runAsync(() -> {
            if (player.hasPermission(PluginPermissions.VERSION_NOTIFY) && !VersionChecker.isLatest(PluginInfo.VERSION))
                player.sendMessage(messageService.resolveMessageWithPrefix("version.notify"));

            //Create player file and set power if player does not exist.
            if (!super.getPlugin().getPlayerManager().getFactionPlayer(player.uniqueId()).isPresent())
                super.getPlugin().getPlayerManager().addPlayer(player.uniqueId(), player.name()); //Maybe we could add loadCache method instead?

            super.getPlugin().getPowerManager().startIncreasingPower(player.uniqueId());

            //Check if the world that player is connecting to is already in the config file
            if (!this.protectionConfig.getDetectedWorldNames().contains(WorldUtil.getPlainWorldName(player.world())))
                this.protectionConfig.addWorld(WorldUtil.getPlainWorldName(player.world()));

            //Send motd
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
            if(optionalPlayerFaction.isPresent() && !optionalPlayerFaction.get().getMessageOfTheDay().equals(""))
            {
                player.sendMessage(messageService.resolveMessageWithPrefix("motd.notify", optionalPlayerFaction.get().getName(), optionalPlayerFaction.get().getMessageOfTheDay()));
            }
        });

        clearPvpLoggerObjectives(player);
    }

    private void clearPvpLoggerObjectives(ServerPlayer player)
    {
        CompletableFuture.runAsync(() ->
        {
            super.getPlugin().getPVPLogger().removePlayer(player);
        });
    }
}
