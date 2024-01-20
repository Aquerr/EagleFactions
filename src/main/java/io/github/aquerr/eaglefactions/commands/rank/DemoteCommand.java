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
//import net.kyori.adventure.audience.Audience;
//import org.checkerframework.checker.nullness.qual.Nullable;
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
//public class DemoteCommand extends AbstractCommand
//{
//    private final MessageService messageService;
//
//    public DemoteCommand(final EagleFactions plugin)
//    {
//        super(plugin);
//        this.messageService = plugin.getMessageService();
//    }
//
//    @Override
//    public CommandResult execute(final CommandContext context) throws CommandException
//    {
//        final FactionPlayer demotedPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());
//        final Faction demotedPlayerFaction = demotedPlayer.getFaction()
//                .orElseThrow(() -> messageService.resolveExceptionWithMessage("error.general.player-is-not-in-faction"));
//
//        if(!isServerPlayer(context.cause().audience()))
//            return demoteByConsole(context.cause().audience(), demotedPlayer, demotedPlayerFaction);
//
//        ServerPlayer sourcePlayer = requirePlayerSource(context);
//        final Faction playerFaction = requirePlayerFaction(sourcePlayer);
//        if (!isSameFaction(demotedPlayerFaction, playerFaction))
//        {
//            throw messageService.resolveExceptionWithMessage("error.general.this-player-is-not-in-your-faction");
//        }
//        return tryDemotePlayer(playerFaction, sourcePlayer, demotedPlayer);
//    }
//
//    private boolean isSameFaction(Faction faction1, Faction faction2)
//    {
//        return faction1 != null
//                && faction2 != null
//                && faction1.getName().equals(faction2.getName());
//    }
//
//    private CommandResult tryDemotePlayer(final Faction faction,
//                                          final ServerPlayer sourcePlayer,
//                                          final FactionPlayer demotedPlayer) throws CommandException
//    {
//        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer.user());
//        final List<Rank> sourcePlayerRanks = faction.getPlayerRanks(sourcePlayer.uniqueId());
//        final List<Rank> targetPlayerRanks = demotedPlayer.getFactionRank().orElse(null);
//
//        if (hasAdminMode)
//        {
//            if (faction.getLeader().getUniqueId().equals(demotedPlayer.getUniqueId()))
//            {
//                return demoteLeader(sourcePlayer, demotedPlayer, faction);
//            }
//            else
//            {
//                return demotePlayer(sourcePlayer, demotedPlayer);
//            }
//        }
//
//        if (!canManageRanks(sourcePlayerRank))
//            throw messageService.resolveExceptionWithMessage("error.general.you-dont-have-access-to-do-this");
//
//        List<Rank> demotableRanks = getDemotableRanksForRank(faction, sourcePlayerRank);
//        if (!demotableRanks.contains(targetPlayerRank))
//            throw messageService.resolveExceptionWithMessage("error.command.demote.you-cant-demote-this-player-more");
//
//        return demotePlayer(sourcePlayer, demotedPlayer);
//    }
//
//    private boolean canManageRanks(Rank sourcePlayerRank)
//    {
//        return sourcePlayerRank != null && sourcePlayerRank.getPermissions().contains(FactionPermission.MANAGE_RANKS);
//    }
//
//    private CommandResult demoteByConsole(final Audience audience,
//                                          final FactionPlayer demotedPlayer,
//                                          final Faction demotedPlayerFaction) throws CommandException
//    {
//        if (demotedPlayer.getUniqueId().equals(demotedPlayerFaction.getLeader().getUniqueId()))
//        {
//            return demoteLeader(audience, demotedPlayer, demotedPlayerFaction);
//        }
//
//        try
//        {
//            final Rank demotedTo = super.getPlugin().getRankManager().demotePlayer(null, demotedPlayer);
//            audience.sendMessage(messageService.resolveMessageWithPrefix("command.demote.you-demoted-player-to-rank", demotedPlayer.getName(), demotedTo.getDisplayName()));
//        }
//        catch (PlayerNotInFactionException ignored)
//        {
//
//        }
//        return CommandResult.success();
//    }
//
//    private CommandResult demotePlayer(final ServerPlayer demotedBy, final FactionPlayer demotedPlayer)
//    {
//        try
//        {
//            final Rank demotedTo = getPlugin().getRankManager().demotePlayer(demotedBy, demotedPlayer);
//            demotedBy.sendMessage(messageService.resolveMessageWithPrefix("command.demote.you-demoted-player-to-rank", demotedPlayer.getName(), demotedTo.getDisplayName()));
//        }
//        catch (PlayerNotInFactionException ignored)
//        {
//        }
//        return CommandResult.success();
//    }
//
//    private CommandResult demoteLeader(Audience context, FactionPlayer demotedPlayer, Faction demotedPlayerFaction)
//    {
//        super.getPlugin().getRankManager().setLeader(demotedPlayer, demotedPlayerFaction);
//        context.sendMessage(messageService.resolveMessageWithPrefix("command.demote.you-demoted-player-to-rank", demotedPlayer.getName(), messageService.resolveComponentWithMessage("rank.officer")));
//        return CommandResult.success();
//    }
//
//    private List<Rank> getDemotableRanksForRank(Faction faction, Rank demoterRank)
//    {
//        if (demoterRank == null)
//            return Collections.emptyList();
//
//        return faction.getRanks().stream()
//                .filter(rank -> rank.getLadderPosition() < demoterRank.getLadderPosition())
//                .collect(Collectors.toList());
//    }
//}
