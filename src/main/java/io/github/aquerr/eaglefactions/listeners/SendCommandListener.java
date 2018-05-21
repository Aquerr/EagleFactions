package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SendCommandListener
{
    @Listener(order = Order.EARLY)
    public void onCommandSend(SendCommandEvent event, @Root Player player)
    {
        if (EagleFactions.getEagleFactions().getPVPLogger().isActive() && EagleFactions.getEagleFactions().getPVPLogger().shouldBlockCommand(player, event.getCommand() + " " + event.getArguments()))
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT));
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.TIME_LEFT + " ", TextColors.YELLOW, EagleFactions.getEagleFactions().getPVPLogger().getPlayerBlockTime(player) + " " + PluginMessages.SECONDS));
            event.setCancelled(true);
            return;
        }
    }
}
