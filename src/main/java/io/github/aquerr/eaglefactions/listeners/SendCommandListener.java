package io.github.aquerr.eaglefactions.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class SendCommandListener extends AbstractListener
{
    public SendCommandListener(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onCommandSend(final ExecuteCommandEvent.Pre event, final @Root ServerPlayer player)
    {
        if (EagleFactionsPlugin.getPlugin().getPVPLogger().isActive() && EagleFactionsPlugin.getPlugin().getPVPLogger().shouldBlockCommand(player, event.command() + " " + event.arguments()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT, NamedTextColor.RED)));
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(MessageLoader.parseMessage(Messages.TIME_LEFT_NUMBER_SECONDS, NamedTextColor.RED, ImmutableMap.of(Placeholders.NUMBER, Component.text(super.getPlugin().getPVPLogger().getPlayerBlockTime(player), NamedTextColor.YELLOW)))));
            event.setCancelled(true);
        }
    }
}
