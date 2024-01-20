package io.github.aquerr.eaglefactions.commands.rank;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class CreateRankCommand extends AbstractCommand
{
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final MessageService messageService;
    private final RankManager rankManager;

    public CreateRankCommand(EagleFactions plugin)
    {
        super(plugin);
        this.permsManager = plugin.getPermsManager();
        this.playerManager = plugin.getPlayerManager();
        this.messageService = plugin.getMessageService();
        this.rankManager = plugin.getRankManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer serverPlayer = requirePlayerSource(context);
        Faction faction = requirePlayerFaction(serverPlayer);
        String rankName = context.requireOne(Parameter.string().key("rank_name").build());
        int ladderPosition = context.one(Parameter.integerNumber().key("ladder_position").build()).orElse(1);

        if (!playerManager.hasAdminMode(serverPlayer.user())
                && !permsManager.hasPermission(serverPlayer.uniqueId(), faction, FactionPermission.MANAGE_RANKS))
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }

        if (faction.getRanks().stream().anyMatch(rank -> rank.getName().equalsIgnoreCase(rankName)))
        {
            throw messageService.resolveExceptionWithMessage("error.command.rank.create.rank-already-exists");
        }

        try
        {
            this.rankManager.createRank(faction, rankName, ladderPosition);
            serverPlayer.sendMessage(messageService.resolveMessageWithPrefix("command.rank.create.rank-has-been-created"));
        }
        catch (Exception exception)
        {
            throw messageService.resolveExceptionWithMessageAndThrowable("error.general.something-went-wrong", exception);
        }
        return CommandResult.success();
    }
}
