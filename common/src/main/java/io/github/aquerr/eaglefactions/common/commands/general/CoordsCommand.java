package io.github.aquerr.eaglefactions.common.commands.general;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.util.ParticlesUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.*;

public class CoordsCommand extends AbstractCommand
{
    public CoordsCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.getOne(Text.of("faction"));
        if (!(source instanceof Player))
        {
            if(!optionalFaction.isPresent())
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You must specify faction name!"));
            return showCoordsList(source, getTeamCoordsList(null, optionalFaction.get()));
        }
        else
        {
            final Player player = (Player)source;
            final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            if(optionalFaction.isPresent())
            {
                final Faction faction = optionalFaction.get();
                if(super.getPlugin().getPlayerManager().hasAdminMode(player) || (optionalPlayerFaction.isPresent() && optionalPlayerFaction.get().getName().equals(faction.getName())))
                    return showCoordsList(player, getTeamCoordsList(player, faction));
                else throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
            }

            if(!optionalPlayerFaction.isPresent())
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

            final Faction playerFaction = optionalPlayerFaction.get();
            return showCoordsList(player, getTeamCoordsList(player, playerFaction));
        }
    }

    private List<Text> getTeamCoordsList(final Player player, final Faction faction)
    {
        final List<Text> teamCoords = new ArrayList<>();

        //Get nearest claim if player is not null
        if (player != null)
        {
            final Claim claim = getNearestClaim(player, faction);
            if (claim != null)
            {
                final Vector3d blockPosition = ParticlesUtil.getChunkCenter(player.getWorld(), claim.getChunkPosition());
                final Text textBuilder = Text.builder()
                        .append(Text.of(Messages.FACTION + ": " + blockPosition))
                        .build();

                teamCoords.add(textBuilder);
            }
        }

        if(faction.getHome() != null)
        {
            final Optional<World> optionalHomeWorld = Sponge.getServer().getWorld(faction.getHome().getWorldUUID());
            final Text textBuilder = Text.builder()
                    .append(Text.of( Messages.FACTIONS_HOME + ": " + (optionalHomeWorld.map(World::getName).orElse("Unknown World")) + '|' + faction.getHome().getBlockPosition().toString()))
                    .build();

            teamCoords.add(textBuilder);
        }

        final Optional<Player> leader = getPlugin().getPlayerManager().getPlayer(faction.getLeader());
        if(leader.isPresent())
        {
            final Text textBuilder = Text.builder()
                    .append(Text.of(Messages.LEADER + ": " + leader.get().getName() + " " + leader.get().getLocation().getBlockPosition().toString()))
                    .build();

            teamCoords.add(textBuilder);
        }

        for (final UUID officerUUID: faction.getOfficers())
        {
            final Optional<Player> officer = getPlugin().getPlayerManager().getPlayer(officerUUID);
            if(officer.isPresent())
            {
                Text textBuilder = Text.builder()
                        .append(Text.of(Messages.OFFICER + ": " + officer.get().getName() + " " + officer.get().getLocation().getBlockPosition().toString()))
                        .build();

                teamCoords.add(textBuilder);
            }
        }

        for (final UUID memberUUID: faction.getMembers())
        {
            final Optional<Player> member = getPlugin().getPlayerManager().getPlayer(memberUUID);
            if(member.isPresent())
            {
                Text textBuilder = Text.builder()
                        .append(Text.of(Messages.MEMBER + ": " + member.get().getName() + " " + member.get().getLocation().getBlockPosition().toString()))
                        .build();

                teamCoords.add(textBuilder);
            }
        }

        for (final UUID recruitUUID: faction.getRecruits())
        {
            final Optional<Player> recruit = getPlugin().getPlayerManager().getPlayer(recruitUUID);
            if(recruit.isPresent())
            {
                Text textBuilder = Text.builder()
                        .append(Text.of(Messages.RECRUIT + ": " + recruit.get().getName() + " " + recruit.get().getLocation().getBlockPosition().toString()))
                        .build();

                teamCoords.add(textBuilder);
            }
        }
        return teamCoords;
    }

    private Claim getNearestClaim(final Player player, final Faction faction)
    {
        final UUID worldUUID = player.getWorld().getUniqueId();
        final Vector3i chunkPosition = player.getLocation().getChunkPosition();
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

    private CommandResult showCoordsList(final CommandSource commandSource, final List<Text> teamCoords) throws CommandException
    {
        final PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        final PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, Messages.TEAM_COORDS)).contents(teamCoords);
        paginationBuilder.sendTo(commandSource);
        return CommandResult.success();
    }
}
