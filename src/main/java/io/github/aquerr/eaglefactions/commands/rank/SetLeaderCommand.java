package io.github.aquerr.eaglefactions.commands.rank;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SetLeaderCommand extends AbstractCommand
{
    private final MessageService messageService;

    public SetLeaderCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer newLeaderPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        final Faction newLeaderPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(newLeaderPlayer.getUniqueId()).orElse(null);

        if (newLeaderPlayerFaction == null || !newLeaderPlayerFaction.getName().equals(playerFaction.getName()))
            throw messageService.resolveExceptionWithMessage("error.general.this-player-is-not-in-your-faction");

        if (super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
        {
            if(playerFaction.getLeader().equals(newLeaderPlayer.getUniqueId()))
                throw messageService.resolveExceptionWithMessage("error.command.set-leader.you-are-already-the-leader-of-this-faction");

            super.getPlugin().getRankManager().setLeader(newLeaderPlayer, playerFaction);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.set-leader.you-set-player-as-your-new-leader", newLeaderPlayer.getName()));
            return CommandResult.success();
        }
        else if (playerFaction.getLeader().equals(player.uniqueId()))
        {
            if(playerFaction.getLeader().equals(newLeaderPlayer.getUniqueId()))
                throw messageService.resolveExceptionWithMessage("error.command.set-leader.you-are-already-the-leader-of-this-faction");

            super.getPlugin().getRankManager().setLeader(newLeaderPlayer, playerFaction);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.set-leader.you-set-player-as-your-new-leader", newLeaderPlayer.getName()));
            return CommandResult.success();
        }
        else
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }
    }
}
