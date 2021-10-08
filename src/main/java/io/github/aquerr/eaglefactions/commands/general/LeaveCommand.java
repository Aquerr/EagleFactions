package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;

public class LeaveCommand extends AbstractCommand
{
    public LeaveCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Player player = requirePlayerSource(source);
        final Faction faction = requirePlayerFaction(player);
        if (faction.getLeader().equals(player.getUniqueId()))
        {
            if (super.getPlugin().getPlayerManager().hasAdminMode(player))
                return leaveFaction(player, faction, true);
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_LEAVE_YOUR_FACTION_BECAUSE_YOU_ARE_ITS_LEADER + " " + Messages.DISBAND_YOUR_FACTION_OR_SET_SOMEONE_AS_LEADER));
        }
        return leaveFaction(player, faction, false);
    }

    private CommandResult leaveFaction(final Player player, final Faction faction, boolean hasAdminMode)
    {
        final boolean isCancelled = EventRunner.runFactionLeaveEventPre(player, faction);
        if (isCancelled)
            return CommandResult.success();

        if (hasAdminMode)
        {
            super.getPlugin().getRankManager().setLeader(null, faction);
        }
        else
        {
            super.getPlugin().getFactionLogic().leaveFaction(player.getUniqueId(), faction.getName());
        }

        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_LEFT_FACTION, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));

        EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.getUniqueId());
        EagleFactionsPlugin.CHAT_LIST.remove(player.getUniqueId());
        EventRunner.runFactionLeaveEventPost(player, faction);
        return CommandResult.success();
    }
}
