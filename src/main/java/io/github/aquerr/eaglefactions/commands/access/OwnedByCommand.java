package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
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

public class OwnedByCommand extends AbstractCommand
{
    public OwnedByCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer factionPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        if(!(isPlayer(context)))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND, NamedTextColor.RED)));

        final ServerPlayer player = (ServerPlayer)context.cause().audience();

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        final Faction playerFaction = optionalPlayerFaction.get();

        // Access can be run only by leader and officers
        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, NamedTextColor.RED)));

        // Get claim at player's location
        return showOwnedBy(player, factionPlayer, playerFaction);
    }

    private CommandResult showOwnedBy(final Player sourcePlayer, final FactionPlayer targetPlayer, final Faction faction)
    {
        final List<TextComponent> resultList = new ArrayList<>();
        final Set<Claim> claims = faction.getClaims();

        for (final Claim claim : claims)
        {
            if (!claim.getOwners().contains(targetPlayer.getUniqueId()))
                continue;

            final TextComponent claimHoverInfo = Component.empty();
            claimHoverInfo.append(Component.text("Accessible by faction: ", NamedTextColor.GOLD).append(Component.text(claim.isAccessibleByFaction()).append(Component.newline())));
            final List<String> ownersNames = claim.getOwners().stream()
                    .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                    .filter(Optional::isPresent)
                    .map(factionPlayer -> factionPlayer.get().getName())
                    .collect(Collectors.toList());
            claimHoverInfo.append(Component.text("Owners: ", NamedTextColor.GOLD).append(Component.text(String.join(", ", ownersNames))));

            final TextComponent textComponent = Component.empty();
            final Optional<ServerWorld> serverWorld = Sponge.server().worldManager().worlds().stream()
                    .filter(world -> world.uniqueId().equals(claim.getWorldUUID()))
                    .findFirst();
            Component worldName = Component.empty();
            if (serverWorld.isPresent())
                worldName = serverWorld.get().properties().displayName().get();
            textComponent.append(Component.text("- ").append(Component.text("World: ", NamedTextColor.YELLOW).append(worldName.color(NamedTextColor.GREEN)).append(Component.text(" | ").append(Component.text("Chunk: ", NamedTextColor.YELLOW).append(Component.text(claim.getChunkPosition().toString(), NamedTextColor.GREEN)))
                    .hoverEvent(claimHoverInfo))));
            resultList.add(textComponent);
            spawnParticlesInClaim(claim);
        }

        final PaginationList paginationList = PaginationList.builder()
                .padding(Component.text("="))
                .title(Component.text("Claims List", NamedTextColor.YELLOW))
                .contents(resultList.toArray(new Component[0]))
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
