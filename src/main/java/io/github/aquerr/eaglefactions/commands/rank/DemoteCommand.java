package io.github.aquerr.eaglefactions.commands.rank;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.exception.PlayerNotInFactionException;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DemoteCommand extends AbstractCommand
{
    private final MessageService messageService;

    public DemoteCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer demotedPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());
        final Faction demotedPlayerFaction = demotedPlayer.getFaction()
                .orElseThrow(() -> messageService.resolveExceptionWithMessage("error.general.player-is-not-in-faction"));

        if(!isServerPlayer(context.cause().audience()))
            return demoteByConsole(context.cause().audience(), demotedPlayer, demotedPlayerFaction);

        ServerPlayer sourcePlayer = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(sourcePlayer);
        if (!isSameFaction(demotedPlayerFaction, playerFaction))
        {
            throw messageService.resolveExceptionWithMessage("error.general.this-player-is-not-in-your-faction");
        }
        return tryDemotePlayer(playerFaction, sourcePlayer, demotedPlayer);
    }

    private boolean isSameFaction(Faction faction1, Faction faction2)
    {
        return faction1 != null
                && faction2 != null
                && faction1.getName().equals(faction2.getName());
    }

    private CommandResult tryDemotePlayer(final Faction faction, final ServerPlayer sourcePlayer, final FactionPlayer demotedPlayer) throws CommandException
    {
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer.user());
        final FactionMemberType sourcePlayerMemberType = faction.getPlayerMemberType(sourcePlayer.uniqueId());
        final FactionMemberType currentRole = demotedPlayer.getFactionRole();

        if (hasAdminMode)
        {
            if (currentRole == FactionMemberType.RECRUIT)
                throw messageService.resolveExceptionWithMessage("error.command.demote.you-cant-demote-this-player-more");

            else if (currentRole == FactionMemberType.LEADER)
            {
                return demoteLeader(sourcePlayer, demotedPlayer, faction);
            }

            return demotePlayer(sourcePlayer, demotedPlayer);
        }

        List<FactionMemberType> demotableRoles = getDemotableRolesForRole(sourcePlayerMemberType);
        if (!demotableRoles.contains(currentRole))
            throw messageService.resolveExceptionWithMessage("error.command.demote.you-cant-demote-this-player-more");

        return demotePlayer(sourcePlayer, demotedPlayer);
    }

    private CommandResult demoteByConsole(final Audience audience, final FactionPlayer demotedPlayer, final Faction demotedPlayerFaction) throws CommandException
    {
        FactionMemberType currentRole = demotedPlayer.getFactionRole();

        if (currentRole == FactionMemberType.LEADER)
        {
            return demoteLeader(audience, demotedPlayer, demotedPlayerFaction);
        }

        if (currentRole == FactionMemberType.RECRUIT)
            throw messageService.resolveExceptionWithMessage("error.command.demote.you-cant-demote-this-player-more");

        try
        {
            final FactionMemberType demotedTo = super.getPlugin().getRankManager().demotePlayer(null, demotedPlayer);
            audience.sendMessage(messageService.resolveMessageWithPrefix("command.demote.you-demoted-player-to-rank", demotedPlayer.getName(), demotedTo.name()));
        }
        catch (PlayerNotInFactionException ignored)
        {
        }
        return CommandResult.success();
    }

    private CommandResult demotePlayer(final ServerPlayer demotedBy, final FactionPlayer demotedPlayer)
    {
        try
        {
            final FactionMemberType demotedTo = getPlugin().getRankManager().demotePlayer(demotedBy, demotedPlayer);
            demotedBy.sendMessage(messageService.resolveMessageWithPrefix("command.demote.you-demoted-player-to-rank", demotedPlayer.getName(), demotedTo.name()));
        }
        catch (PlayerNotInFactionException ignored)
        {
        }
        return CommandResult.success();
    }

    private CommandResult demoteLeader(Audience context, FactionPlayer demotedPlayer, Faction promotedPlayerFaction)
    {
        super.getPlugin().getRankManager().setLeader(null, promotedPlayerFaction);
        context.sendMessage(messageService.resolveMessageWithPrefix("command.demote.you-demoted-player-to-rank", demotedPlayer.getName(), messageService.resolveComponentWithMessage("rank.officer")));
        return CommandResult.success();
    }

    private List<FactionMemberType> getDemotableRolesForRole(FactionMemberType factionMemberType)
    {
        if (factionMemberType != FactionMemberType.LEADER && factionMemberType != FactionMemberType.OFFICER)
            return Collections.emptyList();

        //In case we want to add more roles in the future (probably, we will)
        List<FactionMemberType> roles = new ArrayList<>(Arrays.asList(FactionMemberType.values()));
        roles.remove(FactionMemberType.ALLY);
        roles.remove(FactionMemberType.TRUCE);
        roles.remove(FactionMemberType.RECRUIT);
        roles.remove(FactionMemberType.NONE);

        if (factionMemberType == FactionMemberType.LEADER)
        {
            roles.remove(FactionMemberType.LEADER);
        }
        else
        {
            roles.remove(FactionMemberType.LEADER);
            roles.remove(FactionMemberType.OFFICER);
        }
        return roles;
    }
}
