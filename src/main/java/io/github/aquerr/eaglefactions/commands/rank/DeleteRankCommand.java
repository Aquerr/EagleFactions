package io.github.aquerr.eaglefactions.commands.rank;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class DeleteRankCommand extends AbstractCommand
{
    private final MessageService messageService;
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final RankManager rankManager;

    public DeleteRankCommand(EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
        this.permsManager = plugin.getPermsManager();
        this.playerManager = plugin.getPlayerManager();
        this.rankManager = plugin.getRankManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        Rank rank = context.requireOne(EagleFactionsCommandParameters.factionRank());
        ServerPlayer serverPlayer = requirePlayerSource(context);
        Faction faction = requirePlayerFaction(serverPlayer);

        if (!playerManager.hasAdminMode(serverPlayer.user())
                && permsManager.hasPermission(serverPlayer.uniqueId(), faction, FactionPermission.MANAGE_RANKS))
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }

        if (faction.getRanks().stream().noneMatch(factionRank -> factionRank.getName().equalsIgnoreCase(rank.getName())))
        {
            throw messageService.resolveExceptionWithMessage("error.command.rank.delete.rank-does-not-exist");
        }

        try
        {
            this.rankManager.deleteRank(faction, rank);
            serverPlayer.sendMessage(messageService.resolveMessageWithPrefix("command.rank.delete.rank-has-been-deleted"));
        }
        catch (Exception exception)
        {
            throw messageService.resolveExceptionWithMessageAndThrowable("error.general.something-went-wrong", exception);
        }

        return CommandResult.success();
    }
}
