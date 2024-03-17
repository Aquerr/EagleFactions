package io.github.aquerr.eaglefactions.commands.rank;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;

public class AssignRankCommand extends AbstractCommand
{
    private final PermsManager permsManager;
    private final MessageService messageService;
    private final RankManager rankManager;
    private final PlayerManager playerManager;

    public AssignRankCommand(EagleFactions plugin)
    {
        super(plugin);
        this.permsManager = plugin.getPermsManager();
        this.messageService = plugin.getMessageService();
        this.rankManager = plugin.getRankManager();
        this.playerManager = plugin.getPlayerManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer player = requirePlayerSource(context);
        Faction faction = requirePlayerFaction(player);
        FactionPlayer targetPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());
        Rank requestedRank = context.requireOne(EagleFactionsCommandParameters.factionRank());

        boolean hasAdminMode = playerManager.hasAdminMode(player.user());
        if (!hasAdminMode && !permsManager.hasPermission(player.uniqueId(), faction, FactionPermission.ASSIGN_RANKS))
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }

        if (!faction.containsPlayer(targetPlayer.getUniqueId()))
            throw messageService.resolveExceptionWithMessage("error.general.this-player-is-not-in-your-faction");

        List<Rank> possibleRanks = RankManagerImpl.getEditableRanks(
                faction,
                player.uniqueId(),
                hasAdminMode
        );

        if (possibleRanks.stream().noneMatch(factionRank -> factionRank.isSameRank(requestedRank)))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        try
        {
            this.rankManager.assignRank(faction, targetPlayer, requestedRank);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.assign-rank.success"));
        }
        catch (Exception exception)
        {
            throw messageService.resolveExceptionWithMessageAndThrowable("error.general.something-went-wrong", exception);
        }

        return CommandResult.success();
    }
}
