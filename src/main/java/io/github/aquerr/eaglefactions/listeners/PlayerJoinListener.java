package io.github.aquerr.eaglefactions.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class PlayerJoinListener extends AbstractListener
{
    private final ProtectionConfig protectionConfig;

    public PlayerJoinListener(final EagleFactions plugin)
    {
        super(plugin);
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(final ServerSideConnectionEvent.Join event, final @Root ServerPlayer player)
    {
        CompletableFuture.runAsync(() -> {
            if (player.hasPermission(PluginPermissions.VERSION_NOTIFY) && !VersionChecker.isLatest(PluginInfo.VERSION))
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.A_NEW_VERSION_OF + " ", GREEN)).append(Component.text("Eagle Factions", AQUA)).append(Component.text(" " + Messages.IS_AVAILABLE, GREEN)));

            //Create player file and set power if player does not exist.
            if (!super.getPlugin().getPlayerManager().getFactionPlayer(player.uniqueId()).isPresent())
                super.getPlugin().getPlayerManager().addPlayer(player.uniqueId(), player.name()); //Maybe we could add loadCache method instead?

//            super.getPlugin().getPlayerManager().addPlayer(player.getUniqueId(), player.getName());

//            super.getPlugin().getPlayerManager().updatePlayerName(player.getUniqueId(), player.getName());
            super.getPlugin().getPowerManager().startIncreasingPower(player.uniqueId());


            //Check if the world that player is connecting to is already in the config file
            //TODO: To Test
            if (!this.protectionConfig.getDetectedWorldNames().contains(player.world().key().asString()))
                this.protectionConfig.addWorld(player.world().key().asString());

            //Send motd
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
            if(optionalPlayerFaction.isPresent() && !optionalPlayerFaction.get().getMessageOfTheDay().equals(""))
            {
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_MESSAGE_OF_THE_DAY, WHITE, ImmutableMap.of(Placeholders.FACTION_NAME, Component.text(optionalPlayerFaction.get().getName(), GOLD)))).append(Component.text(optionalPlayerFaction.get().getMessageOfTheDay())));
            }
        });
    }
}
