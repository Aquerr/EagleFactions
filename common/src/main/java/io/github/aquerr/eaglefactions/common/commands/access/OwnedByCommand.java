package io.github.aquerr.eaglefactions.common.commands.access;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class OwnedByCommand extends AbstractCommand
{
    public OwnedByCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
    {
        final FactionPlayer factionPlayer = args.requireOne(Text.of("player"));

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        // Access can be run only by leader and officers
        if (!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));

        // Get claim at player's location
        return showOwnedBy(player, factionPlayer, playerFaction);
    }

    private CommandResult showOwnedBy(final Player sourcePlayer, final FactionPlayer targetPlayer, final Faction faction)
    {
        final List<Text> resultList = new ArrayList<>();
        final Set<Claim> claims = faction.getClaims();

        for (final Claim claim : claims)
        {
            if (!claim.getOwners().contains(targetPlayer.getUniqueId()))
                continue;

            final Text.Builder claimHoverInfo = Text.builder();
            claimHoverInfo.append(Text.of(TextColors.GOLD, "Accessible by faction: ", TextColors.RESET, claim.isAccessibleByFaction(), "\n"));
            final List<String> ownersNames = claim.getOwners().stream()
                    .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                    .filter(Optional::isPresent)
                    .map(factionPlayer -> factionPlayer.get().getName())
                    .collect(Collectors.toList());
            claimHoverInfo.append(Text.of(TextColors.GOLD, "Owners: ", TextColors.RESET, String.join(", ", ownersNames)));

            final Text.Builder textBuilder = Text.builder();
            final Optional<World> world = Sponge.getServer().getWorld(claim.getWorldUUID());
            String worldName = "";
            if (world.isPresent())
                worldName = world.get().getName();
            textBuilder.append(Text.of("- ", TextColors.YELLOW, "World: " , TextColors.GREEN, worldName, TextColors.RESET, " | ", TextColors.YELLOW, "Chunk: ", TextColors.GREEN, claim.getChunkPosition()))
                    .onHover(TextActions.showText(claimHoverInfo.build()));
            resultList.add(textBuilder.build());
            spawnParticlesInClaim(claim);
        }

        final PaginationList paginationList = PaginationList.builder().padding(Text.of("=")).title(Text.of(TextColors.YELLOW, "Claims List")).contents(resultList).linesPerPage(10).build();
        paginationList.sendTo(sourcePlayer);
        return CommandResult.success();
    }

    private void spawnParticlesInClaim(final Claim claim)
    {
        ParticlesUtil.spawnAddAccessParticles(claim);
    }
}
