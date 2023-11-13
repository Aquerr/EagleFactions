package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public abstract class AbstractListener
{
    private final EagleFactions plugin;

    protected AbstractListener(EagleFactions plugin){
        this.plugin = plugin;
    }

    public EagleFactions getPlugin()
    {
        return plugin;
    }

    protected Optional<User> getUserFromEvent(Event event)
    {
        if(event.cause().containsType(ServerPlayer.class))
        {
            return event.cause().first(ServerPlayer.class).map(ServerPlayer::user);
        }
        else if(event.cause().containsType(User.class))
        {
            return event.cause().first(User.class);
        }
        return Optional.empty();
    }

    protected void printDebugMessageForUser(@Nullable User user, LocatableBlock locatableBlock, ServerLocation serverLocation, Event event)
    {
        logDebug(event.toString());

        if (user == null)
            return;

        if (EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            ServerPlayer player = user.player().orElse(null);
            if (player != null)
            {
                if (locatableBlock != null)
                {
                    player.sendMessage(linear(PluginInfo.PLUGIN_PREFIX, text("LocatableBlock: ", BLUE), locatableBlock.blockState().type().asComponent().color(GOLD)));
                }
                if (locatableBlock != null)
                {
                    player.sendMessage(linear(PluginInfo.PLUGIN_PREFIX, text("SourceBlock: ", BLUE), serverLocation.blockType().asComponent().color(GOLD)));
                }
                player.sendMessage(linear(PluginInfo.PLUGIN_PREFIX, text("Event: ", BLUE), text(event.toString(), GOLD)));
            }
        }
    }

    protected void printDebugMessageForUser(@Nullable User user, BlockSnapshot blockSnapshot, Event event)
    {
        logDebug(event.toString());

        if (user == null)
            return;

        if (EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.uniqueId()))
        {
            ServerPlayer player = user.player().orElse(null);
            if (player != null)
            {
                if (blockSnapshot != null)
                {
                    player.sendMessage(linear(PluginInfo.PLUGIN_PREFIX, text("BlockSnapshot: ", BLUE), blockSnapshot.state().type().asComponent().color(GOLD)));
                }
                player.sendMessage(linear(PluginInfo.PLUGIN_PREFIX, text("Event: ", BLUE), text(event.toString(), GOLD)));
            }
        }
    }

    protected boolean isTriggeredByCommandBlock(Event event)
    {
        return event.cause().containsType(CommandBlock.class);
    }

    protected void logDebug(String message)
    {
        Logger logger = EagleFactionsPlugin.getPlugin().getLogger();
        if (EagleFactionsPlugin.getPlugin().getLogger().isDebugEnabled())
            logger.debug(getClass().getName() + ": " + message);
    }
}
