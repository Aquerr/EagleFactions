package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command used for showing list of cliams that belongs to the player's faction.
 *
 * This command can be used by admin.
 */
public class ClaimsListCommand extends AbstractCommand
{
    public ClaimsListCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());

        if (optionalFaction.isPresent())
        {
            final Faction faction = optionalFaction.get();
            if (context.cause().audience() instanceof Server)
            {
                showClaimsList(context.cause().audience(), faction);
                return CommandResult.success();
            }
            else if (isPlayer(context))
            {
                final ServerPlayer player = (ServerPlayer)context.cause().audience();
                if (super.getPlugin().getPlayerManager().hasAdminMode(player))
                {
                    showClaimsList(context.cause().audience(), faction);
                    return CommandResult.success();
                }
                final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
                if (!optionalPlayerFaction.isPresent())
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));
                final Faction playerFaction = optionalPlayerFaction.get();
                if (!faction.getName().equals(playerFaction.getName()))
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS, NamedTextColor.RED)));

                //At this point we know that player belongs to the choosen faction.
                showClaimsList(player, playerFaction);
            }
            else throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS, NamedTextColor.RED)));
        }

        final ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalFactionPlayer = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (!optionalFactionPlayer.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));
        final Faction playerFaction = optionalFactionPlayer.get();
        showClaimsList(player, playerFaction);
        return CommandResult.success();
    }

    private void showClaimsList(final Audience audience, final Faction faction)
    {
        final List<TextComponent> resultList = new LinkedList<>();
        final Set<Claim> claims = faction.getClaims();

        for (final Claim claim : claims)
        {
            final TextComponent claimHoverInfo = Component.empty()
                    .append(Component.text("Accessible by faction: ", NamedTextColor.GOLD))
                    .append(Component.text(String.valueOf(claim.isAccessibleByFaction())))
                    .append(Component.newline());
            final List<String> ownersNames = claim.getOwners().stream()
                    .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                    .filter(Optional::isPresent)
                    .map(factionPlayer -> factionPlayer.get().getName())
                    .collect(Collectors.toList());
            claimHoverInfo.append(Component.text("Owners: ", NamedTextColor.GOLD))
                    .append(Component.text(String.join(", ", ownersNames)));

            final Optional<ServerWorld> serverWorld = Sponge.server().worldManager().worlds().stream()
                    .filter(world -> world.uniqueId().equals(claim.getWorldUUID()))
                    .findFirst();
            Component worldName = Component.empty();
            if (serverWorld.isPresent())
                worldName = serverWorld.get().properties().displayName().get();
            final TextComponent textComponent = Component.empty()
                    .append(Component.text("- "))
                    .append(Component.text("World: ", NamedTextColor.YELLOW))
                    .append(worldName.color(NamedTextColor.GREEN))
                    .append(Component.text(" | "))
                    .append(Component.text("Chunk: ", NamedTextColor.YELLOW))
                    .append(Component.text(claim.getChunkPosition().toString(), NamedTextColor.GREEN))
                    .hoverEvent(claimHoverInfo);
            resultList.add(textComponent);
        }

        final PaginationList paginationList = PaginationList.builder()
                .padding(Component.text("="))
                .title(Component.text("Claims List", NamedTextColor.YELLOW))
                .contents(resultList.toArray(new Component[0]))
                .linesPerPage(10)
                .build();
        paginationList.sendTo(audience);
    }
}
