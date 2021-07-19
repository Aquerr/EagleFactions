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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
            final ServerPlayer serverPlayer = requirePlayerSource(context);
            final Faction playerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(serverPlayer.uniqueId())
                    .orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED))));
            selfInfo(context, playerFaction);
        }
        return CommandResult.success();
    }

    private void selfInfo(final CommandContext context, final Faction faction) throws CommandException
    {
        if (!context.hasPermission(PluginPermissions.INFO_COMMAND) && !context.hasPermission(PluginPermissions.INFO_COMMAND_SELF))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION, NamedTextColor.RED)));
        showFactionInfo(context, faction);
    }
    
    private void otherInfo(final CommandContext context, final Faction faction)
    {
        if(context.hasPermission(PluginPermissions.INFO_COMMAND) || context.hasPermission(PluginPermissions.INFO_COMMAND_SELF) || context.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS))
        {
            //Check permissions
            if((!context.hasPermission(PluginPermissions.INFO_COMMAND) && !context.hasPermission(PluginPermissions.INFO_COMMAND_SELF)) && (context.cause().audience() instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) context.cause().audience()).uniqueId()).isPresent() && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)context.cause().audience()).uniqueId()).get().getName().equals(faction.getName())))
            {
                context.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION, NamedTextColor.RED)));
            }
            else if((!context.hasPermission(PluginPermissions.INFO_COMMAND) && !context.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS)) && (context.cause().audience() instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) context.cause().audience()).uniqueId()).isPresent() && !getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)context.cause().audience()).uniqueId()).get().getName().equals(faction.getName())))
            {
                context.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_OTHER_FACTIONS, NamedTextColor.RED)));
            }
            else
            {
                showFactionInfo(context, faction);
            }
        }
        else
        {
            context.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_DONT_HAVE_PERMISSIONS_TO_USE_THIS_COMMAND, NamedTextColor.RED)));
        }
    }

    private void showFactionInfo(final CommandContext context, final Faction faction)
    {
        final List<TextComponent> factionInfo = new LinkedList<>();

        String leaderName = "";
        if(faction.getLeader() != null && !faction.getLeader().equals(new UUID(0,0)))
        {
            final Optional<FactionPlayer> optionalFactionPlayer = super.getPlugin().getPlayerManager().getFactionPlayer(faction.getLeader());
            if (optionalFactionPlayer.isPresent())
                leaderName = optionalFactionPlayer.get().getName();
        }

        String recruitList = "";
        if(!faction.getRecruits().isEmpty())
        {
        	recruitList = faction.getRecruits().stream()
        			.map(recruit -> getPlugin().getPlayerManager().getFactionPlayer(recruit))
        			.filter(Optional::isPresent).map(Optional::get)
                    .map(FactionPlayer::getName)
        			.collect(Collectors.joining(", "));
        }

        String membersList = "";
        if(!faction.getMembers().isEmpty())
        {
        	membersList = faction.getMembers().stream()
        			.map(member -> getPlugin().getPlayerManager().getFactionPlayer(member))
        			.filter(Optional::isPresent).map(Optional::get)
                    .map(FactionPlayer::getName)
        			.collect(Collectors.joining(", "));
        }

        String officersList = "";
        if(!faction.getOfficers().isEmpty()) {
        	officersList = faction.getOfficers().stream()
        			.map(officer -> getPlugin().getPlayerManager().getPlayer(officer))
        			.filter(Optional::isPresent).map(Optional::get)
                    .map(Player::name)
        			.collect(Collectors.joining(", "));		
        }

        String trucesList = "";
        if(!faction.getTruces().isEmpty())
        {
            trucesList = String.join(", ", faction.getTruces());
        }

        String alliancesList = "";
        if(!faction.getAlliances().isEmpty())
        {
        	alliancesList = String.join(", ", faction.getAlliances());
        }

        String enemiesList = "";
        if(!faction.getEnemies().isEmpty())
        {
        	enemiesList = String.join(", ", faction.getEnemies());
        }


        TextComponent info = Component.empty()
                .append(Component.text(Messages.NAME + ": ", NamedTextColor.AQUA).append(Component.text(faction.getName(), NamedTextColor.GOLD)).append(Component.newline()))
                .append(Component.text(Messages.TAG + ": ", NamedTextColor.AQUA).append(faction.getTag().toBuilder().color(NamedTextColor.GOLD).build()).append(Component.newline())
                .append(Component.text(Messages.LAST_ONLINE + ": ", NamedTextColor.AQUA).append(lastOnline(faction)).append(Component.newline()))
                .append(Component.text(Messages.DESCRIPTION + ": ", NamedTextColor.AQUA).append(Component.text(faction.getDescription(), NamedTextColor.GOLD)).append(Component.newline())))
                .append(Component.text(Messages.MOTD + ": ", NamedTextColor.AQUA).append(Component.text(faction.getMessageOfTheDay(), NamedTextColor.GOLD)).append(Component.newline()))
                .append(Component.text(Messages.PUBLIC + ": ", NamedTextColor.AQUA).append(Component.text(faction.isPublic(), NamedTextColor.GOLD)).append(Component.newline()))
                .append(Component.text(Messages.LEADER + ": ", NamedTextColor.AQUA).append(Component.text(leaderName, NamedTextColor.GOLD)).append(Component.newline()))
                .append(Component.text(Messages.OFFICERS + ": ", NamedTextColor.AQUA).append(Component.text(officersList, NamedTextColor.GOLD)).append(Component.newline()))
                .append(Component.text(Messages.TRUCES + ": ", NamedTextColor.AQUA).append(Component.text(trucesList, NamedTextColor.GOLD)).append(Component.newline()))
                .append(Component.text(Messages.ALLIANCES + ": ", NamedTextColor.AQUA).append(Component.text(alliancesList, NamedTextColor.BLUE)).append(Component.newline()))
                .append(Component.text(Messages.ENEMIES + ": ", NamedTextColor.AQUA).append(Component.text(enemiesList, NamedTextColor.RED)).append(Component.newline()))
                .append(Component.text(Messages.MEMBERS + ": ", NamedTextColor.AQUA).append(Component.text(membersList, NamedTextColor.GREEN)).append(Component.newline()))
                .append(Component.text(Messages.RECRUITS + ": ", NamedTextColor.AQUA).append(Component.text(recruitList, NamedTextColor.GREEN)).append(Component.newline()))
                .append(Component.text(Messages.POWER + ": ", NamedTextColor.AQUA).append(Component.text(super.getPlugin().getPowerManager().getFactionPower(faction) + "/" + super.getPlugin().getPowerManager().getFactionMaxPower(faction), NamedTextColor.GOLD).append(Component.newline())))
                .append(Component.text(Messages.CLAIMS + ": ", NamedTextColor.AQUA).append(Component.text(faction.getClaims().size() + "/" + super.getPlugin().getPowerManager().getFactionMaxClaims(faction), NamedTextColor.GOLD)));

        factionInfo.add(info);

        PaginationList.Builder paginationBuilder = PaginationList.builder()
                .title(Component.text(Messages.FACTION_INFO, NamedTextColor.GREEN))
                .contents(factionInfo.toArray(new Component[0]));
        paginationBuilder.sendTo(context.cause().audience());
    }

    private TextComponent lastOnline(final Faction faction)
    {
        if(getPlugin().getFactionLogic().hasOnlinePlayers(faction))
            return Component.text(Messages.NOW, NamedTextColor.GREEN);

        final Date date = Date.from(faction.getLastOnline());
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String formattedDate = formatter.format(date);
        return Component.text(formattedDate, NamedTextColor.RED);
    }
}