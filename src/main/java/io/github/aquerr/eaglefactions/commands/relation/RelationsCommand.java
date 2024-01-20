package io.github.aquerr.eaglefactions.commands.relation;

import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class RelationsCommand extends AbstractCommand
{
    private final FactionLogic factionLogic;
    private final MessageService messageService;

    public RelationsCommand(EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Optional<Faction> faction = context.one(EagleFactionsCommandParameters.optionalFaction());
        if (faction.isPresent())
        {
            if (isPlayerFaction(faction.get(), context))
            {
                showSelfFactionRelations(context, faction.get());
            }
            else
            {
                showOtherFactionRelations(context, faction.get());
            }
        }
        else
        {
            final ServerPlayer player = requirePlayerSource(context);
            final Faction playerFaction = requirePlayerFaction(player);
            showSelfFactionRelations(context, playerFaction);
        }
        return CommandResult.success();
    }

    private boolean isPlayerFaction(Faction faction, CommandContext context) throws CommandException
    {
        if (!isServerPlayer(context.cause().audience()))
            return false;
        Faction playerFaction = this.factionLogic.getFactionByPlayerUUID(requirePlayerSource(context).uniqueId())
                .orElse(null);
        return playerFaction != null && playerFaction.getName().equalsIgnoreCase(faction.getName());
    }

    private void showOtherFactionRelations(CommandContext context, Faction faction) throws CommandException
    {
        if (!context.hasPermission(PluginPermissions.LIST_RELATIONS_COMMAND) && !context.hasPermission(PluginPermissions.LIST_RELATIONS_OTHERS_COMMAND))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        showRelations(context, faction);
    }

    private void showSelfFactionRelations(CommandContext context, Faction faction) throws CommandException
    {
        if (!context.hasPermission(PluginPermissions.LIST_RELATIONS_COMMAND) && !context.hasPermission(PluginPermissions.LIST_RELATIONS_SELF_COMMAND))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        showRelations(context, faction);
    }

    private void showRelations(CommandContext context, Faction faction)
    {
        final List<Component> relationsInfo = new ArrayList<>();

        Component trucesList = empty();
        if(!faction.getTruces().isEmpty())
        {
            trucesList = buildRelationList(faction.getTruces());
        }

        Component alliancesList = empty();
        if(!faction.getAlliances().isEmpty())
        {
            alliancesList = buildRelationList(faction.getAlliances());
        }

        Component enemiesList = empty();
        if(!faction.getEnemies().isEmpty())
        {
            enemiesList = buildRelationList(faction.getEnemies());
        }

        Component relations = text()
                .append(messageService.resolveComponentWithMessage("command.relations.truces", trucesList.color(GRAY))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.relations.alliances", alliancesList.color(BLUE))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.relations.enemies", enemiesList.color(RED)))
                .build();

        relationsInfo.add(relations);
        showAsPagination(context, relationsInfo, faction);
    }

    private void showAsPagination(CommandContext context, List<Component> relationsInfo, Faction faction)
    {
        PaginationService paginationService = Sponge.serviceProvider().paginationService();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(messageService.resolveComponentWithMessage("command.relations.header", faction.getName()))
                .contents(relationsInfo);
        paginationBuilder.sendTo(context.cause().audience());
    }

    private Component buildRelationList(Set<String> relations)
    {
        return Component.join(JoinConfiguration.separator(text(",")), relations.stream()
                .map(this::buildClickableFactionName)
                .collect(Collectors.toList()));
    }

    private TextComponent buildClickableFactionName(String factionName)
    {
        return text(factionName)
                .hoverEvent(HoverEvent.showText(messageService.resolveComponentWithMessage("command.relations.click-to-view-information-about-faction")))
                .clickEvent(ClickEvent.runCommand("/f info " + factionName));
    }
}
