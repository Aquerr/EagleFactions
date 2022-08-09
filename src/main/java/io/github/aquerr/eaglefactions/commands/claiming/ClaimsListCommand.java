package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
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
import static net.kyori.adventure.text.LinearComponents.linear;

/**
 * Command used for showing list of claims which belongs to player's faction.
 *
 * This command can be used by admin.
 */
public class ClaimsListCommand extends AbstractCommand
{
    private final MessageService messageService;

    public ClaimsListCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
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
                    throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY);
                final Faction playerFaction = optionalPlayerFaction.get();
                if (!faction.getName().equals(playerFaction.getName()))
                    throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

                //At this point we know that player belongs to the choosen faction.
                showClaimsList(player, playerFaction);
            }
            else throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
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
            final List<String> ownersNames = claim.getOwners().stream()
                    .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                    .filter(Optional::isPresent)
                    .map(factionPlayer -> factionPlayer.get().getName())
                    .collect(Collectors.toList());
            final Component claimHoverInfo = linear(messageService.resolveComponentWithMessage("command.access.accessible-by-faction", claim.isAccessibleByFaction()), newline(),
                    messageService.resolveComponentWithMessage("command.access.owners", String.join(", ", ownersNames)));

            final Optional<ServerWorld> world = WorldUtil.getWorldByUUID(claim.getWorldUUID());
            String worldName = "";
            //TODO: To test...
            if (world.isPresent())
                worldName = WorldUtil.getPlainWorldName(world.get());
            resultList.add(messageService.resolveComponentWithMessage("command.access.access-line", worldName, claim.getChunkPosition().toString())
                    .hoverEvent(HoverEvent.showText(claimHoverInfo)));
        }

        final PaginationList paginationList = PaginationList.builder()
                .padding(messageService.resolveComponentWithMessage("command.claim-list.padding-character"))
                .title(messageService.resolveComponentWithMessage("command.claim-list.header"))
                .contents(resultList)
                .linesPerPage(10)
                .build();
        paginationList.sendTo(audience);
    }
}
