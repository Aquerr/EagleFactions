package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.identity.Identity;
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
import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class InfoCommand extends AbstractCommand
{
    public InfoCommand(final EagleFactions plugin)
    {
        super(plugin);
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
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION, RED)));
        showFactionInfo(context, faction);
    }
    
    private void otherInfo(final CommandContext source, final Faction faction)
    {
        if(source.hasPermission(PluginPermissions.INFO_COMMAND) || source.hasPermission(PluginPermissions.INFO_COMMAND_SELF) || source.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS))
        {
            //Check permissions
            if((!source.hasPermission(PluginPermissions.INFO_COMMAND) && !source.hasPermission(PluginPermissions.INFO_COMMAND_SELF)) && (source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) source).uniqueId()).isPresent() && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).uniqueId()).get().getName().equals(faction.getName())))
            {
                source.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION, RED)));
            }
            else if((!source.hasPermission(PluginPermissions.INFO_COMMAND) && !source.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS)) && (source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) source).uniqueId()).isPresent() && !getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).uniqueId()).get().getName().equals(faction.getName())))
            {
                source.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_OTHER_FACTIONS, RED)));
            }
            else
            {
                showFactionInfo(source, faction);
            }
        }
        else
        {
            source.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_PERMISSIONS_TO_USE_THIS_COMMAND, RED)));
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
                .append(text(Messages.NAME + ": ", AQUA)).append(text(faction.getName(), GOLD)).append(newline())
                .append(text(Messages.TAG + ": ", AQUA)).append(faction.getTag().color(GOLD)).append(newline())
                .append(text(Messages.LAST_ONLINE + ": ", AQUA)).append(lastOnline(faction)).append(newline())
                .append(text(Messages.DESCRIPTION + ": ", AQUA)).append(text(faction.getDescription(), GOLD)).append(newline())
                .append(text(Messages.MOTD + ": ", AQUA)).append(text(faction.getMessageOfTheDay(), GOLD)).append(newline())
                .append(text(Messages.PUBLIC + ": ", AQUA)).append(text(faction.isPublic(), GOLD)).append(newline())
                .append(text(Messages.LEADER + ": ", AQUA)).append(leaderNameText.color(GOLD)).append(newline())
                .append(text(Messages.OFFICERS + ": ", AQUA)).append(officersList.color(GOLD)).append(newline())
                .append(text(Messages.TRUCES + ": ", AQUA)).append(trucesList.color(GOLD)).append(newline())
                .append(text(Messages.ALLIANCES + ": ", AQUA)).append(alliancesList.color(BLUE)).append(newline())
                .append(text(Messages.ENEMIES + ": ", AQUA)).append(enemiesList.color(RED)).append(newline())
                .append(text(Messages.MEMBERS + ": ", AQUA)).append(membersList.color(GREEN)).append(newline())
                .append(text(Messages.RECRUITS + ": ", AQUA)).append(recruitList.color(GREEN)).append(newline())
                .append(text(Messages.POWER + ": ", AQUA)).append(text(super.getPlugin().getPowerManager().getFactionPower(faction) + "/" + super.getPlugin().getPowerManager().getFactionMaxPower(faction), GOLD).append(newline())
                .append(text(Messages.CLAIMS + ": ", AQUA)).append(text(faction.getClaims().size() + "/" + super.getPlugin().getPowerManager().getFactionMaxClaims(faction), GOLD)))
                .build();

        factionInfo.add(info);

        PaginationService paginationService = Sponge.serviceProvider().paginationService();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(text(Messages.FACTION_INFO, GREEN))
                .contents(factionInfo);
        paginationBuilder.sendTo(source.cause().audience());
    }

    private Component lastOnline(final Faction faction)
    {
        if(getPlugin().getFactionLogic().hasOnlinePlayers(faction))
            return text(Messages.NOW, GREEN);

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