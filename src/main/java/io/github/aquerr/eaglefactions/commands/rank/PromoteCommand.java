//package io.github.aquerr.eaglefactions.commands.rank;
//
//import io.github.aquerr.eaglefactions.api.EagleFactions;
//import io.github.aquerr.eaglefactions.api.entities.Faction;
//import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
//import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
//import io.github.aquerr.eaglefactions.api.entities.Rank;
//import io.github.aquerr.eaglefactions.api.exception.PlayerNotInFactionException;
//import io.github.aquerr.eaglefactions.api.messaging.MessageService;
//import io.github.aquerr.eaglefactions.commands.AbstractCommand;
//import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
//import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
//import net.kyori.adventure.audience.Audience;
//import org.spongepowered.api.command.CommandResult;
//import org.spongepowered.api.command.exception.CommandException;
//import org.spongepowered.api.command.parameter.CommandContext;
//import org.spongepowered.api.entity.living.player.server.ServerPlayer;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//public class PromoteCommand extends AbstractCommand
//{
//    private final MessageService messageService;
//
//    public PromoteCommand(final EagleFactions plugin)
//    {
//        super(plugin);
//        this.messageService = plugin.getMessageService();
//    }
//
//    @Override
//    public CommandResult execute(final CommandContext context) throws CommandException
//    {
//        final FactionPlayer promotedPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());
//        final Faction promotedPlayerFaction = promotedPlayer.getFaction()
//                .orElseThrow(() -> messageService.resolveExceptionWithMessage("error.general.player-is-not-in-faction"));
//
//        if(!isServerPlayer(context.cause().audience()))
//            return promoteByConsole(context.cause().audience(), promotedPlayer, promotedPlayerFaction);
//
//        final ServerPlayer sourcePlayer = requirePlayerSource(context);
//        final Faction playerFaction = requirePlayerFaction(sourcePlayer);
//        if (!isSameFaction(promotedPlayerFaction, playerFaction))
//        {
//            throw messageService.resolveExceptionWithMessage("error.general.this-player-is-not-in-your-faction");
//        }
//        return tryPromotePlayer(playerFaction, sourcePlayer, promotedPlayer);
//    }
//
//    private boolean isSameFaction(Faction faction1, Faction faction2)
//    {
//        return faction1 != null
//                && faction2 != null
//                && faction1.getName().equals(faction2.getName());
//    }
//
//    private CommandResult tryPromotePlayer(final Faction faction, final ServerPlayer sourcePlayer, final FactionPlayer targetPlayer) throws CommandException
//    {
//        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer.user());
//        final Rank sourcePlayerRank = faction.getPlayerRank(sourcePlayer.uniqueId()).orElse(null);
//        final Rank targetPlayerRank = targetPlayer.getFactionRank().orElse(null);
//
//        if (hasAdminMode)
//        {
//            if (faction.getRanks().stream()
//                    .filter(rank -> rank.getLadderPosition() > targetPlayerRank.getLadderPosition())
//                    .count() == 1)
//            {
//                return setAsLeader(sourcePlayer, targetPlayer, faction);
//            }
//
//            return promotePlayer(sourcePlayer,targetPlayer);
//        }
//
//        if (!canManageRanks(sourcePlayerRank))
//            throw messageService.resolveExceptionWithMessage("error.general.you-dont-have-access-to-do-this");
//
//        List<Rank> promotableRoles = RankManagerImpl.getPromotableRanksForRank(faction, sourcePlayerRank, sourcePlayer.uniqueId());
//        if (!promotableRoles.contains(targetPlayerRank))
//            throw messageService.resolveExceptionWithMessage("error.command.promote.you-cant-promote-this-player-more");
//
//        return promotePlayer(sourcePlayer, targetPlayer);
//    }
//
//    private boolean canManageRanks(Rank sourcePlayerRank)
//    {
//        return sourcePlayerRank != null && sourcePlayerRank.getPermissions().contains(FactionPermission.MANAGE_RANKS);
//    }
//
//    private CommandResult promoteByConsole(final Audience audience,
//                                           final FactionPlayer promotedPlayer,
//                                           final Faction promotedPlayerFaction) throws CommandException
//    {
//        Rank currentRank = promotedPlayer.getFactionRank().orElse(null);
//        if (currentRank == null)
//            return CommandResult.success();
//
//        if (promotedPlayerFaction.getRanks().stream()
//                .filter(rank -> rank.getLadderPosition() > currentRank.getLadderPosition())
//                .count() == 1)
//        {
//            return setAsLeader(audience, promotedPlayer, promotedPlayerFaction);
//        }
//
//        try
//        {
//            final Rank promotedTo = super.getPlugin().getRankManager().promotePlayer(null, promotedPlayer);
//            audience.sendMessage(messageService.resolveMessageWithPrefix("command.promote.you-promoted-player-to-rank", promotedPlayer.getName(), promotedTo.getDisplayName()));
//        }
//        catch (PlayerNotInFactionException ignored)
//        {
//        }
//        return CommandResult.success();
//    }
//
//    private CommandResult setAsLeader(Audience audience, FactionPlayer promotedPlayer, Faction promotedPlayerFaction)
//    {
//        super.getPlugin().getRankManager().setLeader(promotedPlayer, promotedPlayerFaction);
//        audience.sendMessage(messageService.resolveMessageWithPrefix("command.promote.you-promoted-player-to-rank", promotedPlayer.getName(), messageService.resolveComponentWithMessage("rank.leader")));
//        return CommandResult.success();
//    }
//
//    private CommandResult promotePlayer(final ServerPlayer promotedBy, final FactionPlayer promotedPlayer)
//    {
//        try
//        {
//            final Rank promotedTo = getPlugin().getRankManager().promotePlayer(promotedBy, promotedPlayer);
//            promotedBy.sendMessage(messageService.resolveMessageWithPrefix("command.promote.you-promoted-player-to-rank", promotedPlayer.getName(), promotedTo.getDisplayName()));
//        }
//        catch (PlayerNotInFactionException ignored)
//        {
//        }
//
//        return CommandResult.success();
//    }
//}
