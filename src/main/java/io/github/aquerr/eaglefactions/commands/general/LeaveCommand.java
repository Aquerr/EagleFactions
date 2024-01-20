package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class LeaveCommand extends AbstractCommand
{
    private final MessageService messageService;

    public LeaveCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction faction = requirePlayerFaction(player);
        if (faction.getLeader().getUniqueId().equals(player.uniqueId()))
        {
            if (super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
                return leaveFaction(player, faction, true);
            throw messageService.resolveExceptionWithMessage("error.command.leave.you-cant-leave-your-faction-because-you-are-its-leader");
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

        player.sendMessage(messageService.resolveMessageWithPrefix("command.leave.you-left-faction", faction.getName()));

        EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
        EagleFactionsPlugin.CHAT_LIST.remove(player.uniqueId());
        EventRunner.runFactionLeaveEventPost(player, faction);
        return CommandResult.success();
    }
}
