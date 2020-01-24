package io.github.aquerr.eaglefactions.common.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SendCommandListener extends AbstractListener
{
    public SendCommandListener(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onCommandSend(final SendCommandEvent event, final @Root Player player)
    {
        if (EagleFactionsPlugin.getPlugin().getPVPLogger().isActive() && EagleFactionsPlugin.getPlugin().getPVPLogger().shouldBlockCommand(player, event.getCommand() + " " + event.getArguments()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT));
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, MessageLoader.parseMessage(Messages.TIME_LEFT_NUMBER_SECONDS, ImmutableMap.of(Placeholders.NUMBER, Text.of(TextColors.YELLOW, super.getPlugin().getPVPLogger().getPlayerBlockTime(player))))));
            event.setCancelled(true);
        }
    }
}
