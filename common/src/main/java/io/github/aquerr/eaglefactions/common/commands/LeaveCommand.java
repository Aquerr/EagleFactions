package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction faction = optionalPlayerFaction.get();
        if (faction.getLeader().equals(player.getUniqueId()))
        {
            if (super.getPlugin().getPlayerManager().hasAdminMode(player))
                return leaveFaction(player, faction, true);
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_LEAVE_YOUR_FACTION_BECAUSE_YOU_ARE_ITS_LEADER + " " + Messages.DISBAND_YOUR_FACTION_OR_SET_SOMEONE_AS_LEADER));
        }
        return leaveFaction(player, optionalPlayerFaction.get(), false);
    }

    private CommandResult leaveFaction(final Player player, final Faction faction, boolean isLeader)
    {
        final boolean isCancelled = EventRunner.runFactionLeaveEvent(player, faction);
        if (isCancelled)
            return CommandResult.success();

        if (isLeader)
        {
            super.getPlugin().getFactionLogic().setLeader(new UUID(0, 0), faction.getName());
        }
        else
        {
            super.getPlugin().getFactionLogic().leaveFaction(player.getUniqueId(), faction.getName());
        }

        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_LEFT_FACTION, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));

        EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.getUniqueId());
        EagleFactionsPlugin.CHAT_LIST.remove(player.getUniqueId());

        return CommandResult.success();
    }
}
