package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.events.FactionLeaveEventImpl;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class LeaveCommand extends AbstractCommand
{
    public LeaveCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        if (optionalPlayerFaction.get().getLeader().equals(player.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_LEAVE_YOUR_FACTION_BECAUSE_YOU_ARE_ITS_LEADER + " " + PluginMessages.DISBAND_YOUR_FACTION_OR_SET_SOMEONE_AS_LEADER));

        //TODO: Add listener that will inform players in a faction that someone has left their faction.

        final boolean isCancelled = EventRunner.runFactionLeaveEvent(player, optionalPlayerFaction.get());
        if (isCancelled)
            return CommandResult.success();

        super.getPlugin().getFactionLogic().leaveFaction(player.getUniqueId(), optionalPlayerFaction.get().getName());

        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, PluginMessages.YOU_LEFT_FACTION + " ", TextColors.GOLD, optionalPlayerFaction.get().getName()));

        EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.getUniqueId());
        EagleFactionsPlugin.CHAT_LIST.remove(player.getUniqueId());

        return CommandResult.success();
    }
}
