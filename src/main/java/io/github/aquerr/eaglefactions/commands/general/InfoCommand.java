package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class InfoCommand extends AbstractCommand
{
    private final MessageService messageService;

    public InfoCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> faction = context.one(EagleFactionsCommandParameters.faction());
        if (faction.isPresent())
        {
            otherInfo(context, faction.get());
        }
        else
        {
            final ServerPlayer player = requirePlayerSource(context);
            final Faction playerFaction = requirePlayerFaction(player);
            selfInfo(context, playerFaction);
        }
        return CommandResult.success();
    }

    private void selfInfo(final CommandContext context, final Faction faction) throws CommandException
    {
        if (!context.hasPermission(PluginPermissions.INFO_COMMAND) && !context.hasPermission(PluginPermissions.INFO_COMMAND_SELF))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        showFactionInfo(context, faction);
    }
    
    private void otherInfo(final CommandContext source, final Faction faction) throws CommandException
    {
        if(source.hasPermission(PluginPermissions.INFO_COMMAND) || source.hasPermission(PluginPermissions.INFO_COMMAND_SELF) || source.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS))
        {
            //Check permissions
            if((!source.hasPermission(PluginPermissions.INFO_COMMAND) && !source.hasPermission(PluginPermissions.INFO_COMMAND_SELF)) && (source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) source).uniqueId()).isPresent() && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).uniqueId()).get().getName().equals(faction.getName())))
            {
                throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
            }
            else if((!source.hasPermission(PluginPermissions.INFO_COMMAND) && !source.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS)) && (source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) source).uniqueId()).isPresent() && !getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).uniqueId()).get().getName().equals(faction.getName())))
            {
                throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
            }
            else
            {
                showFactionInfo(source, faction);
            }
        }
        else
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }
    }

    private void showFactionInfo(final CommandContext source, final Faction faction)
    {
        final List<Component> factionInfo = new ArrayList<>();

        Component leaderNameText = empty();
        if(faction.getLeader() != null && !faction.getLeader().equals(new UUID(0,0)))
        {
            final Optional<FactionPlayer> optionalFactionPlayer = super.getPlugin().getPlayerManager().getFactionPlayer(faction.getLeader());
            if (optionalFactionPlayer.isPresent())
            {
                leaderNameText = buildClickablePlayerNickname(optionalFactionPlayer.get());
            }
        }

        Component recruitList = empty();
        if(!faction.getRecruits().isEmpty())
        {
            recruitList = buildPlayerList(faction.getRecruits());
        }

        Component membersList = empty();
        if(!faction.getMembers().isEmpty())
        {
        	membersList = buildPlayerList(faction.getMembers());
        }

        Component officersList = empty();
        if(!faction.getOfficers().isEmpty())
        {
        	officersList = buildPlayerList(faction.getOfficers());
        }

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

        Component info = text()
                .append(messageService.resolveComponentWithMessage("command.info.name", faction.getName())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.tag", faction.getTag().color(GOLD))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.last-online", lastOnline(faction))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.description", faction.getDescription())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.motd", faction.getMessageOfTheDay())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.public", faction.isPublic())).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.leader", leaderNameText.color(GOLD))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.officers", officersList.color(GOLD))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.truces", trucesList.color(GOLD))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.alliances", alliancesList.color(BLUE))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.enemies", enemiesList.color(RED))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.members", membersList.color(GREEN))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.recruits", recruitList.color(GREEN))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.power", super.getPlugin().getPowerManager().getFactionPower(faction) + "/" + super.getPlugin().getPowerManager().getFactionMaxPower(faction))).append(newline())
                .append(messageService.resolveComponentWithMessage("command.info.claims", faction.getClaims().size() + "/" + super.getPlugin().getPowerManager().getFactionMaxClaims(faction)))
                .build();

        factionInfo.add(info);

        PaginationService paginationService = Sponge.serviceProvider().paginationService();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(messageService.resolveComponentWithMessage("command.info.faction-info-header"))
                .contents(factionInfo);
        paginationBuilder.sendTo(source.cause().audience());
    }

    private Component lastOnline(final Faction faction)
    {
        if(getPlugin().getFactionLogic().hasOnlinePlayers(faction))
            return messageService.resolveComponentWithMessage("command.info.now");

        final Date date = Date.from(faction.getLastOnline());
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String formattedDate = formatter.format(date);
        return text(formattedDate, RED);
    }

    private Component buildPlayerList(Collection<UUID> playerUUIDs)
    {
        return Component.join(JoinConfiguration.separator(text(",")), playerUUIDs.stream()
                .map(recruit -> getPlugin().getPlayerManager().getFactionPlayer(recruit))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::buildClickablePlayerNickname)
                .collect(Collectors.toList()));
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
                .hoverEvent(HoverEvent.showText(text("Click to view information about the faction", BLUE)))
                .clickEvent(ClickEvent.runCommand("/f info " + factionName));
    }

    private TextComponent buildClickablePlayerNickname(FactionPlayer factionPlayer)
    {
        return text(factionPlayer.getName())
                .hoverEvent(HoverEvent.showText(text("Click to view information about the player", BLUE)))
                .clickEvent(ClickEvent.runCommand("/f player " + factionPlayer.getName()));
    }
}