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

public class PromoteCommand extends AbstractCommand
{
    private final MessageService messageService;

    public PromoteCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer promotedPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());
        final Faction promotedPlayerFaction = promotedPlayer.getFaction()
                .orElseThrow(() -> messageService.resolveExceptionWithMessage("error.general.player-is-not-in-faction"));

        if(!isServerPlayer(context.cause().audience()))
            return promoteByConsole(context.cause().audience(), promotedPlayer, promotedPlayerFaction);

        final ServerPlayer sourcePlayer = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(sourcePlayer);
        if (!isSameFaction(promotedPlayerFaction, playerFaction))
        {
            throw messageService.resolveExceptionWithMessage("error.general.this-player-is-not-in-your-faction");
        }
        return tryPromotePlayer(playerFaction, sourcePlayer, promotedPlayer);
    }

    private boolean isSameFaction(Faction faction1, Faction faction2)
    {
        return faction1 != null
                && faction2 != null
                && faction1.getName().equals(faction2.getName());
    }

    private CommandResult tryPromotePlayer(final Faction faction, final ServerPlayer sourcePlayer, final FactionPlayer targetPlayer) throws CommandException
    {
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer.user());
        final FactionMemberType sourcePlayerMemberType = faction.getPlayerMemberType(sourcePlayer.uniqueId());
        final FactionMemberType targetPlayerMemberType = targetPlayer.getFactionRole();

        if (hasAdminMode)
        {
            if (targetPlayerMemberType == FactionMemberType.OFFICER)
            {
                return setAsLeader(sourcePlayer, targetPlayer, faction);
            }

            if (targetPlayerMemberType == FactionMemberType.LEADER)
                throw messageService.resolveExceptionWithMessage("error.command.promote.you-cant-promote-this-player-more");

            return promotePlayer(sourcePlayer,targetPlayer);
        }

        List<FactionMemberType> promotableRoles = getPromotableRolesForRole(sourcePlayerMemberType);
        if (!promotableRoles.contains(targetPlayerMemberType))
            throw messageService.resolveExceptionWithMessage("error.command.promote.you-cant-promote-this-player-more");

        return promotePlayer(sourcePlayer, targetPlayer);
    }

    private CommandResult promoteByConsole(final Audience audience, final FactionPlayer promotedPlayer, final Faction promotedPlayerFaction) throws CommandException
    {
        FactionMemberType currentRole = promotedPlayer.getFactionRole();
        if (currentRole == FactionMemberType.OFFICER)
        {
            return setAsLeader(audience, promotedPlayer, promotedPlayerFaction);
        }

        if (currentRole == FactionMemberType.LEADER)
        {
            throw messageService.resolveExceptionWithMessage("error.command.promote.you-cant-promote-this-player-more");
        }

        try
        {
            final FactionMemberType promotedTo = super.getPlugin().getRankManager().promotePlayer(null, promotedPlayer);
            audience.sendMessage(messageService.resolveMessageWithPrefix("command.promote.you-promoted-player-to-rank", promotedPlayer.getName(), promotedTo.name()));
        }
        catch (PlayerNotInFactionException ignored)
        {
        }
        return CommandResult.success();
    }

    private CommandResult setAsLeader(Audience audience, FactionPlayer promotedPlayer, Faction promotedPlayerFaction)
    {
        super.getPlugin().getRankManager().setLeader(promotedPlayer, promotedPlayerFaction);
        audience.sendMessage(messageService.resolveMessageWithPrefix("command.promote.you-promoted-player-to-rank", promotedPlayer.getName(), messageService.resolveComponentWithMessage("rank.leader")));
        return CommandResult.success();
    }

    private CommandResult promotePlayer(final ServerPlayer promotedBy, final FactionPlayer promotedPlayer)
    {
        try
        {
            final FactionMemberType promotedTo = getPlugin().getRankManager().promotePlayer(promotedBy, promotedPlayer);
            promotedBy.sendMessage(messageService.resolveMessageWithPrefix("command.promote.you-promoted-player-to-rank", promotedPlayer.getName(), promotedTo.name()));
        }
        catch (PlayerNotInFactionException ignored)
        {
        }

        return CommandResult.success();
    }

    private List<FactionMemberType> getPromotableRolesForRole(FactionMemberType factionMemberType)
    {
        if (factionMemberType != FactionMemberType.LEADER && factionMemberType != FactionMemberType.OFFICER)
            return Collections.emptyList();

        //In case we want to add more roles in the future (probably, we will)
        List<FactionMemberType> roles = new ArrayList<>(Arrays.asList(FactionMemberType.values()));
        roles.remove(FactionMemberType.ALLY);
        roles.remove(FactionMemberType.TRUCE);
        roles.remove(FactionMemberType.OFFICER);
        roles.remove(FactionMemberType.LEADER);
        roles.remove(FactionMemberType.NONE);

        if (factionMemberType == FactionMemberType.OFFICER)
        {
            roles.remove(FactionMemberType.MEMBER);
        }
        return roles;
    }
}
