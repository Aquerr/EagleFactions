package io.github.aquerr.eaglefactions.commands.rank;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.util.Nameable;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.event.HoverEvent.showText;

public class ListRanksCommand extends AbstractCommand
{
    private final MessageService messageService;
    private final FactionLogic factionLogic;
    private final PlayerManager playerManager;

    public ListRanksCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.playerManager = plugin.getPlayerManager();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Optional<Faction> faction = context.one(EagleFactionsCommandParameters.optionalFaction());
        if (faction.isPresent())
        {
            if (isConsoleOrAdmin(context) || isPlayerFaction(faction.get(), context))
            {
                listRanks(context, faction.get());
            }
            else
            {
                throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
            }
        }
        else
        {
            final ServerPlayer player = requirePlayerSource(context);
            final Faction playerFaction = requirePlayerFaction(player);
            listRanks(context, playerFaction);
        }
        return CommandResult.success();
    }

    private void listRanks(CommandContext context, Faction faction)
    {
        CompletableFuture.runAsync(() ->{
            List<Component> helpList = new LinkedList<>();
            List<Rank> sortedRanks = faction.getRanks().stream()
                    .sorted(Comparator.comparingInt(Rank::getLadderPosition).reversed())
                    .collect(Collectors.toList());

            for(final Rank rank : sortedRanks)
            {
                Component factionHelp =
                        LinearComponents.linear(
                                Component.text(rank.getLadderPosition() + ". ", NamedTextColor.GOLD),
                                Component.text(rank.getName(), NamedTextColor.GOLD).hoverEvent(showText(getRankHoverText(rank))),
                                Component.text(": " + getPlayersTextForRank(faction, rank))
                        );

                helpList.add(factionHelp);
            }

            PaginationList.Builder paginationBuilder = PaginationList.builder()
                    .title(messageService.resolveComponentWithMessage("command.list-ranks.ranks-list.header"))
                    .padding(messageService.resolveComponentWithMessage("command.list.faction-list.padding-character"))
                    .contents(helpList);
            paginationBuilder.sendTo(context.cause().audience());
        });
    }

    private String getPlayersTextForRank(Faction faction, Rank rank)
    {
        return faction.getMembers().stream()
                .filter(factionMember -> factionMember.getRankNames().contains(rank.getName()))
                .map(FactionMember::getUniqueId)
                .map(playerManager::getPlayer)
                .map(player -> player.map(Nameable::name).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
    }

    private Component getRankHoverText(Rank rank)
    {
        return Component.text("Display Name: ").append(LegacyComponentSerializer.legacySection().deserialize(rank.getDisplayName()))
                .append(Component.newline())
                .append(Component.text("Permissions: " + rank.getPermissions().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")), NamedTextColor.BLUE));
    }

    private boolean isConsoleOrAdmin(CommandContext context) throws CommandException
    {
        return !isServerPlayer(context.cause().audience()) || (playerManager.hasAdminMode(requirePlayerSource(context).user()));
    }

    private boolean isPlayerFaction(Faction faction, CommandContext context) throws CommandException
    {
        if (!isServerPlayer(context.cause().audience()))
            return false;
        Faction playerFaction = this.factionLogic.getFactionByPlayerUUID(requirePlayerSource(context).uniqueId())
                .orElse(null);
        return playerFaction != null && playerFaction.getName().equalsIgnoreCase(faction.getName());
    }
}
