package io.github.aquerr.eaglefactions.common.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.PluginPermissions;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import io.github.aquerr.eaglefactions.common.version.VersionChecker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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
    public void onPlayerJoin(final ClientConnectionEvent.Join event, final @Root Player player)
    {
        CompletableFuture.runAsync(() -> {
            if (player.hasPermission(PluginPermissions.VERSION_NOTIFY) && !VersionChecker.isLatest(PluginInfo.VERSION))
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.A_NEW_VERSION_OF + " ", TextColors.AQUA, "Eagle Factions", TextColors.GREEN, " " + Messages.IS_AVAILABLE));

            //Create player file and set power if player does not exist.
            if (!super.getPlugin().getPlayerManager().getFactionPlayer(player.getUniqueId()).isPresent())
                super.getPlugin().getPlayerManager().addPlayer(player.getUniqueId(), player.getName()); //Maybe we could add loadCache method instead?

//            super.getPlugin().getPlayerManager().addPlayer(player.getUniqueId(), player.getName());

//            super.getPlugin().getPlayerManager().updatePlayerName(player.getUniqueId(), player.getName());
            super.getPlugin().getPowerManager().startIncreasingPower(player.getUniqueId());


            //Check if the world that player is connecting to is already in the config file
            if (!this.protectionConfig.getDetectedWorldNames().contains(player.getWorld().getName()))
                this.protectionConfig.addWorld(player.getWorld().getName());

            //Send motd
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            if(optionalPlayerFaction.isPresent() && !optionalPlayerFaction.get().getMessageOfTheDay().equals(""))
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_MESSAGE_OF_THE_DAY, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, optionalPlayerFaction.get().getName()))), Text.of(optionalPlayerFaction.get().getMessageOfTheDay())));
            }
        });
    }
}
