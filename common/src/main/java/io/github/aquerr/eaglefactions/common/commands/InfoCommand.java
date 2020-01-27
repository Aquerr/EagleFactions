package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.PluginPermissions;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Aquerr on 2017-08-03.
 */
public class InfoCommand extends AbstractCommand
{
    public InfoCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<String> optionalFactionName = context.<String>getOne("faction name");
        if (optionalFactionName.isPresent())
        {
            final String rawFactionName = optionalFactionName.get();
            final Faction faction = getPlugin().getFactionLogic().getFactionByName(rawFactionName);

            if (faction == null)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, MessageLoader.parseMessage(Messages.THERE_IS_NO_FACTION_CALLED_FACTION_NAME, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, rawFactionName)))));

            if(source.hasPermission(PluginPermissions.INFO_COMMAND) || source.hasPermission(PluginPermissions.INFO_COMMAND_SELF) || source.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS))
            {
                //Check permissions
                if((!source.hasPermission(PluginPermissions.INFO_COMMAND) && !source.hasPermission(PluginPermissions.INFO_COMMAND_SELF)) && (source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) source).getUniqueId()).isPresent() && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId()).get().getName().equals(faction.getName())))
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION));
                }
                else if((!source.hasPermission(PluginPermissions.INFO_COMMAND) && !source.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS)) && (source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) source).getUniqueId()).isPresent() && !getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId()).get().getName().equals(faction.getName())))
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_OTHER_FACTIONS));
                }
                else
                {
                    showFactionInfo(source, faction);
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_PERMISSIONS_TO_USE_THIS_COMMAND));
            }
        }
        else if(source instanceof Player && super.getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId()).isPresent())
        {
            //Check permissions
            if(source.hasPermission(PluginPermissions.INFO_COMMAND) || source.hasPermission(PluginPermissions.INFO_COMMAND_SELF))
            {
                showFactionInfo(source, super.getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId()).get());
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, Messages.USAGE + " /f info <faction name>"));
        }

        return CommandResult.success();
    }

    private void showFactionInfo(final CommandSource source, final Faction faction)
    {
        final List<Text> factionInfo = new ArrayList<>();

        String leaderName = "";
        if(faction.getLeader() != null && !faction.getLeader().equals(new UUID(0,0)))
        {
            Optional<String> optionalName = getPlugin().getPlayerManager().getPlayerName(faction.getLeader());
            if(optionalName.isPresent())
                leaderName = optionalName.get();
        }

        String recruitList = "";
        if(!faction.getRecruits().isEmpty())
        {
        	recruitList = faction.getRecruits().stream()
        			.map(recruit -> getPlugin().getPlayerManager().getPlayerName(recruit))
        			.filter(Optional::isPresent).map(Optional::get)
        			.collect(Collectors.joining(", "));
        }

        String membersList = "";
        if(!faction.getMembers().isEmpty())
        {
        	membersList = faction.getMembers().stream()
        			.map(member -> getPlugin().getPlayerManager().getPlayerName(member))
        			.filter(Optional::isPresent).map(Optional::get)
        			.collect(Collectors.joining(", "));
        }

        String officersList = "";
        if(!faction.getOfficers().isEmpty()) {
        	officersList = faction.getOfficers().stream()
        			.map(officer -> getPlugin().getPlayerManager().getPlayerName(officer))
        			.filter(Optional::isPresent).map(Optional::get)
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


        Text info = Text.builder()
                .append(Text.of(TextColors.AQUA, Messages.NAME + ": ", TextColors.GOLD, faction.getName() + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.TAG + ": "), faction.getTag().toBuilder().color(TextColors.GOLD).build(), Text.of("\n"))
                .append(Text.of(TextColors.AQUA, Messages.LAST_ONLINE + ": "), lastOnline(faction), Text.of("\n"))
                .append(Text.of(TextColors.AQUA, Messages.DESCRIPTION + ": ", TextColors.GOLD, faction.getDescription() + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.MOTD + ": ", TextColors.GOLD, faction.getMessageOfTheDay() + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.PUBLIC + ": ", TextColors.GOLD, faction.isPublic() + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.LEADER + ": ", TextColors.GOLD, leaderName + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.OFFICERS + ": ", TextColors.GOLD, officersList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.TRUCES + ": ", TextColors.GOLD, trucesList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.ALLIANCES + ": ", TextColors.BLUE, alliancesList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.ENEMIES + ": ", TextColors.RED, enemiesList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.MEMBERS + ": ", TextColors.GREEN, membersList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.RECRUITS + ": ", TextColors.GREEN, recruitList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.POWER + ": ", TextColors.GOLD, super.getPlugin().getPowerManager().getFactionPower(faction) + "/" + super.getPlugin().getPowerManager().getFactionMaxPower(faction) + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.CLAIMS + ": ", TextColors.GOLD, faction.getClaims().size() + "/" + super.getPlugin().getPowerManager().getFactionMaxClaims(faction)))
                .build();

        factionInfo.add(info);

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, Messages.FACTION_INFO)).contents(factionInfo);
        paginationBuilder.sendTo(source);
    }

    private Text lastOnline(final Faction faction)
    {
        if(getPlugin().getFactionLogic().hasOnlinePlayers(faction))
            return Text.of(TextColors.GREEN, Messages.NOW);

        final Date date = Date.from(faction.getLastOnline());
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        final String formattedDate = formatter.format(date);
        return Text.of(TextColors.RED, formattedDate);
    }
}