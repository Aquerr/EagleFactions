package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CoordsCommand extends AbstractCommand
{
    public CoordsCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());
        if (!(context.cause().audience() instanceof ServerPlayer))
        {
            if(!optionalFaction.isPresent())
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text("You must specify faction name!", RED)));
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
                else throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS, RED)));
            }

            if(!optionalPlayerFaction.isPresent())
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, RED)));

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
                final TextComponent textComponent = Component.text(Messages.FACTION, NamedTextColor.AQUA)
                        .append(Component.text(": " + blockPosition, NamedTextColor.GOLD));
                teamCoords.add(textComponent);
            }
        }

        if(faction.getHome() != null)
        {
            final Optional<ServerWorld> optionalHomeWorld = WorldUtil.getWorldByUUID(faction.getHome().getWorldUUID());
            final TextComponent textComponent = Component.text(Messages.FACTIONS_HOME, NamedTextColor.AQUA).append(Component.text(": " + (optionalHomeWorld.map(world -> world.properties().name()).orElse("Unknow World")) + "|" + faction.getHome().getBlockPosition().toString()));
            teamCoords.add(textComponent);
        }

        final Optional<ServerPlayer> leader = getPlugin().getPlayerManager().getPlayer(faction.getLeader());
        if(leader.isPresent())
        {
            final TextComponent textComponent = Component.text(Messages.LEADER, NamedTextColor.AQUA).append(Component.text(": " + leader.get().name() + " " + leader.get().serverLocation().blockPosition().toString(), NamedTextColor.GOLD));
            teamCoords.add(textComponent);
        }

        for (final UUID officerUUID: faction.getOfficers())
        {
            final Optional<ServerPlayer> officer = getPlugin().getPlayerManager().getPlayer(officerUUID);
            if(officer.isPresent())
            {
                final TextComponent textComponent = Component.text(Messages.OFFICER, NamedTextColor.AQUA).append(Component.text(": " + officer.get().name() + " " + officer.get().serverLocation().blockPosition().toString(), NamedTextColor.GOLD));
                teamCoords.add(textComponent);
            }
        }

        for (final UUID memberUUID: faction.getMembers())
        {
            final Optional<ServerPlayer> member = getPlugin().getPlayerManager().getPlayer(memberUUID);
            if(member.isPresent())
            {
                TextComponent textComponent = Component.text(Messages.MEMBER, NamedTextColor.AQUA).append(Component.text(": " + member.get().name() + " " + member.get().serverLocation().blockPosition().toString(), NamedTextColor.GOLD));
                teamCoords.add(textComponent);
            }
        }

        for (final UUID recruitUUID: faction.getRecruits())
        {
            final Optional<ServerPlayer> recruit = getPlugin().getPlayerManager().getPlayer(recruitUUID);
            if(recruit.isPresent())
            {
                TextComponent textComponent = Component.text(Messages.RECRUIT, NamedTextColor.AQUA).append(Component.text(": " + recruit.get().name() + " " + recruit.get().serverLocation().blockPosition().toString(), NamedTextColor.GOLD));
                teamCoords.add(textComponent);
            }
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

    private CommandResult showCoordsList(final Audience audience, final List<Component> teamCoords) throws CommandException
    {
        final PaginationService paginationService = Sponge.serviceProvider().paginationService();
        final PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(text(Messages.TEAM_COORDS, GREEN))
                .contents(teamCoords);
        paginationBuilder.sendTo(audience);
        return CommandResult.success();
    }
}
