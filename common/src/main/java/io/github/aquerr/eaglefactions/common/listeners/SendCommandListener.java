package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
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
    public void onCommandSend(final SendCommandEvent event, @Root Player player)
    {
        if (EagleFactionsPlugin.getPlugin().getPVPLogger().isActive() && EagleFactionsPlugin.getPlugin().getPVPLogger().shouldBlockCommand(player, event.getCommand() + " " + event.getArguments()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT));
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.TIME_LEFT + " ", TextColors.YELLOW, EagleFactionsPlugin.getPlugin().getPVPLogger().getPlayerBlockTime(player) + " " + PluginMessages.SECONDS));
            event.setCancelled(true);
            return;
        }
    }
}
