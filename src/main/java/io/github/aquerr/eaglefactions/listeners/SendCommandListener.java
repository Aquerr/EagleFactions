package io.github.aquerr.eaglefactions.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.logic.CommandBlockerOtherFactionTerritory;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

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
    public void onCommandSend(final ExecuteCommandEvent.Pre event, final @Root ServerPlayer player)
    {
        final String commandWithArgs = event.command() + " " + event.arguments();

        if (this.pvpLogger.isActive() && this.pvpLogger.shouldBlockCommand(player, commandWithArgs))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT, RED)));
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(MessageLoader.parseMessage(Messages.TIME_LEFT_NUMBER_SECONDS, RED, ImmutableMap.of(Placeholders.NUMBER, text(super.getPlugin().getPVPLogger().getPlayerBlockTime(player), YELLOW)))));
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
