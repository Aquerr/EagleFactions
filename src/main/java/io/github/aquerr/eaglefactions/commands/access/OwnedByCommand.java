package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.LinearComponents.linear;

public class OwnedByCommand extends AbstractCommand
{
    private final MessageService messageService;

    public OwnedByCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer factionPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);

        // Access can be run only by leader and officers
        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw this.messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);

        // Get claim at player's location
        return showOwnedBy(player, factionPlayer, playerFaction);
    }

    private CommandResult showOwnedBy(final Player sourcePlayer, final FactionPlayer targetPlayer, final Faction faction)
    {
        final List<Component> resultList = new ArrayList<>();
        final Set<Claim> claims = faction.getClaims();

        for (final Claim claim : claims)
        {
            if (!claim.getOwners().contains(targetPlayer.getUniqueId()))
                continue;

            final List<String> ownersNames = claim.getOwners().stream()
                    .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                    .filter(Optional::isPresent)
                    .map(factionPlayer -> factionPlayer.get().getName())
                    .collect(Collectors.toList());
            final Component claimHoverInfo = linear(messageService.resolveComponentWithMessage("command.access.accessible-by-faction", claim.isAccessibleByFaction()), newline(),
                    messageService.resolveComponentWithMessage("command.access.owners", String.join(", ", ownersNames)));

            final Optional<ServerWorld> world = WorldUtil.getWorldByUUID(claim.getWorldUUID());
            String worldName = "";
            if (world.isPresent())
                worldName = WorldUtil.getPlainWorldName(world.get());

            resultList.add(messageService.resolveComponentWithMessage("command.access.access-line", worldName, claim.getChunkPosition().toString())
                .hoverEvent(HoverEvent.showText(claimHoverInfo)));
            spawnParticlesInClaim(claim);
        }

        final PaginationList paginationList = PaginationList.builder()
                .padding(messageService.resolveComponentWithMessage("command.access.padding-character"))
                .title(messageService.resolveComponentWithMessage("command.access.header"))
                .contents(resultList)
                .linesPerPage(10)
                .build();
        paginationList.sendTo(sourcePlayer);
        return CommandResult.success();
    }

    private void spawnParticlesInClaim(final Claim claim)
    {
        ParticlesUtil.spawnAddAccessParticles(claim);
    }
}
