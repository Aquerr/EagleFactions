package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CoordsCommand extends AbstractCommand
{
    private final MessageService messageService;

    public CoordsCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());
        if (!isServerPlayer(context.cause().audience()))
        {
            if(!optionalFaction.isPresent())
                throw messageService.resolveExceptionWithMessage("error.command.coords.you-must-specify-faction-name");
            return showCoordsList(context.cause().audience(), getTeamCoordsList(null, optionalFaction.get()));
        }
        else
        {
            final ServerPlayer player = (ServerPlayer) context.cause().audience();
            final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
            if(optionalFaction.isPresent())
            {
                final Faction faction = optionalFaction.get();
                if(super.getPlugin().getPlayerManager().hasAdminMode(player.user()) || (optionalPlayerFaction.isPresent() && optionalPlayerFaction.get().getName().equals(faction.getName())))
                    return showCoordsList(player, getTeamCoordsList(player, faction));
                else throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
            }

            if(!optionalPlayerFaction.isPresent())
                throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY);

            final Faction playerFaction = optionalPlayerFaction.get();
            return showCoordsList(player, getTeamCoordsList(player, playerFaction));
        }
    }

    private List<Component> getTeamCoordsList(final ServerPlayer player, final Faction faction)
    {
        final List<Component> teamCoords = new LinkedList<>();

        //Get nearest claim if player is not null
        if (player != null)
        {
            final Claim claim = getNearestClaim(player, faction);
            if (claim != null)
            {
                final Vector3d blockPosition = ParticlesUtil.getChunkCenter(player.world(), claim.getChunkPosition());
                teamCoords.add(messageService.resolveComponentWithMessage("command.coords.faction-coords", blockPosition.toString()));
            }
        }

        if(faction.getHome() != null)
        {
            final Optional<ServerWorld> optionalHomeWorld = WorldUtil.getWorldByUUID(faction.getHome().getWorldUUID());
            final String worldNameAndPos = optionalHomeWorld.map(WorldUtil::getPlainWorldName)
                    .orElse("Unknown World") + "|" + faction.getHome().getBlockPosition().toString();
            teamCoords.add(messageService.resolveComponentWithMessage("command.coords.faction-home-coords", worldNameAndPos));
        }

        final Optional<ServerPlayer> leader = getPlugin().getPlayerManager().getPlayer(faction.getLeader());
        leader.ifPresent(serverPlayer -> teamCoords.add(messageService.resolveComponentWithMessage("command.coords.leader-coords", serverPlayer.name() + " " + serverPlayer.serverLocation().blockPosition().toString())));

        for (final UUID officerUUID: faction.getOfficers())
        {
            final Optional<ServerPlayer> officer = getPlugin().getPlayerManager().getPlayer(officerUUID);
            officer.ifPresent(serverPlayer -> teamCoords.add(messageService.resolveComponentWithMessage("command.coords.officer-coords", serverPlayer.name() + " " + serverPlayer.serverLocation().blockPosition().toString())));
        }

        for (final UUID memberUUID: faction.getMembers())
        {
            final Optional<ServerPlayer> member = getPlugin().getPlayerManager().getPlayer(memberUUID);
            member.ifPresent(serverPlayer -> teamCoords.add(messageService.resolveComponentWithMessage("command.coords.member-coords", serverPlayer.name() + " " + serverPlayer.serverLocation().blockPosition().toString())));
        }

        for (final UUID recruitUUID: faction.getRecruits())
        {
            final Optional<ServerPlayer> recruit = getPlugin().getPlayerManager().getPlayer(recruitUUID);
            recruit.ifPresent(serverPlayer -> teamCoords.add(messageService.resolveComponentWithMessage("command.coords.recruit-coords", serverPlayer.name() + " " + serverPlayer.serverLocation().blockPosition().toString())));
        }
        return teamCoords;
    }

    private Claim getNearestClaim(final ServerPlayer player, final Faction faction)
    {
        final UUID worldUUID = player.world().uniqueId();
        final Vector3i chunkPosition = player.serverLocation().chunkPosition();
        final Set<Claim> claims = faction.getClaims();
        if (claims.isEmpty())
            return null;
        
        Claim nearestClaim = claims.iterator().next();

        for (final Claim claim : claims)
        {
            if (!claim.getWorldUUID().equals(worldUUID))
                continue;

            float distanceToCurrentClaim = nearestClaim.getChunkPosition().distance(chunkPosition);
            float distanceToNextClaim = claim.getChunkPosition().distance(chunkPosition);
            if (distanceToNextClaim < distanceToCurrentClaim)
                nearestClaim = claim;
        }

        if (!nearestClaim.getWorldUUID().equals(worldUUID))
            nearestClaim = null;

        return nearestClaim;
    }

    private CommandResult showCoordsList(final Audience audience, final List<Component> teamCoords)
    {
        final PaginationService paginationService = Sponge.serviceProvider().paginationService();
        final PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(messageService.resolveComponentWithMessage("command.coords.header"))
                .contents(teamCoords);
        paginationBuilder.sendTo(audience);
        return CommandResult.success();
    }
}
