package io.github.aquerr.eaglefactions.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.logic.CommandBlockerOtherFactionTerritory;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SendCommandListener extends AbstractListener
{
    private final PVPLogger pvpLogger;
    private final CommandBlockerOtherFactionTerritory commandBlocker;

    public SendCommandListener(final EagleFactions plugin)
    {
        super(plugin);
        this.pvpLogger = plugin.getPVPLogger();
        this.commandBlocker = new CommandBlockerOtherFactionTerritory(plugin.getFactionLogic(), plugin.getConfiguration().getProtectionConfig().getBlockedCommandsInOtherFactionsTerritory());
    }

    @Listener(order = Order.EARLY)
    public void onCommandSend(final SendCommandEvent event, final @Root Player player)
    {
        final String commandWithArgs = event.getCommand() + " " + event.getArguments();

        if (this.pvpLogger.isActive() && this.pvpLogger.shouldBlockCommand(player, commandWithArgs))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT));
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, MessageLoader.parseMessage(Messages.TIME_LEFT_NUMBER_SECONDS, TextColors.RED, ImmutableMap.of(Placeholders.NUMBER, Text.of(TextColors.YELLOW, super.getPlugin().getPVPLogger().getPlayerBlockTime(player))))));
            event.setCancelled(true);
            return;
        }

        if (this.commandBlocker.shouldBlockCommand(player, commandWithArgs))
        {
            event.setCancelled(true);
            return;
        }
    }
}
