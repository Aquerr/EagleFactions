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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class LeaveCommand extends AbstractCommand
{
    public LeaveCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);
        if (faction.getLeader().equals(player.uniqueId()))
        {
            if (super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
                return leaveFaction(player, faction, true);
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_LEAVE_YOUR_FACTION_BECAUSE_YOU_ARE_ITS_LEADER + " " + Messages.DISBAND_YOUR_FACTION_OR_SET_SOMEONE_AS_LEADER, RED)));
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
            super.getPlugin().getFactionLogic().leaveFaction(player.uniqueId(), faction.getName());
        }

        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_LEFT_FACTION, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(faction.getName(), GOLD)))));

        EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
        EagleFactionsPlugin.CHAT_LIST.remove(player.uniqueId());
        EventRunner.runFactionLeaveEventPost(player, faction);
        return CommandResult.success();
    }
}
