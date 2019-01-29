package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SendCommandListener extends AbstractListener
{
    public SendCommandListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onCommandSend(SendCommandEvent event, @Root Player player)
    {
        if (EagleFactions.getPlugin().getPVPLogger().isActive() && EagleFactions.getPlugin().getPVPLogger().shouldBlockCommand(player, event.getCommand() + " " + event.getArguments()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT));
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.TIME_LEFT + " ", TextColors.YELLOW, EagleFactions.getPlugin().getPVPLogger().getPlayerBlockTime(player) + " " + PluginMessages.SECONDS));
            event.setCancelled(true);
            return;
        }
    }
}
