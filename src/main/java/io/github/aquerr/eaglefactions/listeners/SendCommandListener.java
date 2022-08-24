package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.logic.CommandBlockerOtherFactionTerritory;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class SendCommandListener extends AbstractListener
{
    private final PVPLogger pvpLogger;
    private final CommandBlockerOtherFactionTerritory commandBlocker;
    private final MessageService messageService;

    public SendCommandListener(final EagleFactions plugin)
    {
        super(plugin);
        this.pvpLogger = plugin.getPVPLogger();
        this.commandBlocker = new CommandBlockerOtherFactionTerritory(plugin.getFactionLogic(), plugin.getConfiguration().getProtectionConfig().getBlockedCommandsInOtherFactionsTerritory());
        this.messageService = plugin.getMessageService();
    }

    @Listener(order = Order.EARLY)
    public void onCommandSend(final ExecuteCommandEvent.Pre event, final @Root ServerPlayer player)
    {
        final String commandWithArgs = event.command() + " " + event.arguments();

        if (this.pvpLogger.isActive() && this.pvpLogger.shouldBlockCommand(player, commandWithArgs))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("pvplogger.you-cant-use-this-command-while-being-in-fight")));
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("pvplogger.time-left", super.getPlugin().getPVPLogger().getPlayerBlockTime(player))));
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
