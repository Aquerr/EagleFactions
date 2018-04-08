package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
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
        if (EagleFactions.getEagleFactions().getPVPLogger().isActive() && EagleFactions.getEagleFactions().getPVPLogger().shouldBlockCommands(player))
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't use commands while being in a fight!"));
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Time left: ", TextColors.YELLOW, EagleFactions.getEagleFactions().getPVPLogger().getPlayerBlockTime(player) + "seconds"));
            event.setCancelled(true);
            return;
        }
    }
}
