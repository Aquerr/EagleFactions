package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class NotAccessibleByFactionCommand extends AbstractCommand
{
    public NotAccessibleByFactionCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);

        // Access can be run only by leader and officers
        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));

        // Get claim at player's location
        return showNotAccessibleByFaction(player, playerFaction);
    }

    private CommandResult showNotAccessibleByFaction(final Player sourcePlayer, final Faction faction)
    {
        final List<Component> resultList = new ArrayList<>();
        final Set<Claim> claims = faction.getClaims();

        for (final Claim claim : claims)
        {
            if (claim.isAccessibleByFaction())
                continue;

            final TextComponent.Builder claimHoverInfo = Component.text();
            claimHoverInfo.append(text("Accessible by faction: ", GOLD)).append(text(claim.isAccessibleByFaction(), WHITE)).append(Component.newline());
            final List<String> ownersNames = claim.getOwners().stream()
                    .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                    .filter(Optional::isPresent)
                    .map(factionPlayer -> factionPlayer.get().getName())
                    .collect(Collectors.toList());
            claimHoverInfo.append(text("Owners: ", GOLD)).append(text(String.join(", ", ownersNames), WHITE));

            final TextComponent.Builder textBuilder = Component.text();
            final Optional<ServerWorld> world = WorldUtil.getWorldByUUID(claim.getWorldUUID());
            String worldName = "";
            if (world.isPresent())
                worldName = WorldUtil.getPlainWorldName(world.get());
            textBuilder.append(text("- ")).append(text("World: ", YELLOW)).append(text(worldName, GREEN)).append(text(" | ", WHITE)).append(text("Chunk: ", YELLOW)).append(text(claim.getChunkPosition().toString(), GREEN))
                    .hoverEvent(HoverEvent.showText(claimHoverInfo.build()));
            resultList.add(textBuilder.build());
            spawnParticlesInClaim(claim);
        }

        final PaginationList paginationList = PaginationList.builder()
                .padding(text("="))
                .title(text("Claims List", YELLOW))
                .contents(resultList)
                .linesPerPage(10)
                .build();
        paginationList.sendTo(sourcePlayer);
        return CommandResult.success();
    }

    private void spawnParticlesInClaim(final Claim claim)
    {
        ParticlesUtil.spawnRemoveAccessParticles(claim);
    }
}
