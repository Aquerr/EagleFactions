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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener extends AbstractListener
{
    private final ProtectionConfig protectionConfig;

    public PlayerJoinListener(final EagleFactions plugin)
    {
        super(plugin);
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(final ServerSideConnectionEvent.Join event, @Getter(value = "player") ServerPlayer player)
    {
        CompletableFuture.runAsync(() -> {
            if (player.hasPermission(PluginPermissions.VERSION_NOTIFY) && !VersionChecker.isLatest(PluginInfo.VERSION))
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.A_NEW_VERSION_OF + " ", NamedTextColor.GREEN)).append(Component.text("Eagle Factions", NamedTextColor.AQUA)).append(Component.text(" " + Messages.IS_AVAILABLE, NamedTextColor.GREEN)));

            //Create player file and set power if player does not exist.
            if (!super.getPlugin().getPlayerManager().getFactionPlayer(player.uniqueId()).isPresent())
                super.getPlugin().getPlayerManager().addPlayer(player.uniqueId(), player.name()); //Maybe we could add loadCache method instead?

//            super.getPlugin().getPlayerManager().addPlayer(player.getUniqueId(), player.getName());

//            super.getPlugin().getPlayerManager().updatePlayerName(player.getUniqueId(), player.getName());
            super.getPlugin().getPowerManager().startIncreasingPower(player.uniqueId());


            //Check if the world that player is connecting to is already in the config file
            if (!this.protectionConfig.getDetectedWorldNames().contains(((TextComponent)player.world().properties().displayName().get()).content()))
                this.protectionConfig.addWorld(((TextComponent)player.world().properties().displayName().get()).content());

            //Send motd
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
            if(optionalPlayerFaction.isPresent() && !optionalPlayerFaction.get().getMessageOfTheDay().equals(""))
            {
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_MESSAGE_OF_THE_DAY, NamedTextColor.WHITE, ImmutableMap.of(Placeholders.FACTION_NAME, Component.text(optionalPlayerFaction.get().getName(), NamedTextColor.GOLD))).append(Component.text(optionalPlayerFaction.get().getMessageOfTheDay()))));
            }
        });
    }

    @Listener
    public void onLogin(ServerSideConnectionEvent.Login event)
    {
        System.out.println(event.user());
    }
}
