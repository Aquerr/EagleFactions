package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

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
            final Audience audience = context.cause().audience();
            if (audience instanceof Server)
            {
                showClaimsList(audience, faction);
                return CommandResult.success();
            }
            else if (audience instanceof ServerPlayer)
            {
                final ServerPlayer player = (ServerPlayer) audience;
                if (super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
                {
                    showClaimsList(player, faction);
                    return CommandResult.success();
                }
                final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
                if (!optionalPlayerFaction.isPresent())
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, RED)));
                final Faction playerFaction = optionalPlayerFaction.get();
                if (!faction.getName().equals(playerFaction.getName()))
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS, RED)));

                //At this point we know that player belongs to the choosen faction.
                showClaimsList(player, playerFaction);
            }
            else throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS, RED)));
        }

        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        showClaimsList(player, playerFaction);
        return CommandResult.success();
    }

    private void showClaimsList(final Audience audience, final Faction faction)
    {
        final List<Component> resultList = new ArrayList<>();
        final Set<Claim> claims = faction.getClaims();

        for (final Claim claim : claims)
        {
            final TextComponent.Builder claimHoverInfo = Component.text();
            claimHoverInfo.append(text("Accessible by faction: ", GOLD)).append(text(claim.isAccessibleByFaction(), WHITE)).append(newline());
            final List<String> ownersNames = claim.getOwners().stream()
                    .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                    .filter(Optional::isPresent)
                    .map(factionPlayer -> factionPlayer.get().getName())
                    .collect(Collectors.toList());
            claimHoverInfo.append(text("Owners: ", GOLD)).append(text(String.join(", ", ownersNames), WHITE));

            final TextComponent.Builder textBuilder = Component.text();
            final Optional<ServerWorld> world = WorldUtil.getWorldByUUID(claim.getWorldUUID());
            String worldName = "";
            //TODO: To test...
            if (world.isPresent())
                worldName = world.get().key().asString();
            textBuilder.append(text("- ")).append(text("World: ", YELLOW)).append(text(worldName, GREEN)).append(text(" | ")).append(text("Chunk: ", YELLOW)).append(text(claim.getChunkPosition().toString(), GREEN))
                    .hoverEvent(HoverEvent.showText(claimHoverInfo.build()));
            resultList.add(textBuilder.build());
        }

        final PaginationList paginationList = PaginationList.builder()
                .padding(text("="))
                .title(text("Claims List", YELLOW))
                .contents(resultList)
                .linesPerPage(10)
                .build();
        paginationList.sendTo(audience);
    }
}
