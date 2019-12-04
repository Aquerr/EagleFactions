package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.PluginPermissions;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
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
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName + "!"));
            }
            else
            {
                if(source.hasPermission(PluginPermissions.INFO_COMMAND) || source.hasPermission(PluginPermissions.INFO_COMMAND_SELF) || source.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS))
                {
                    //Check permissions
                    if((!source.hasPermission(PluginPermissions.INFO_COMMAND) && !source.hasPermission(PluginPermissions.INFO_COMMAND_SELF)) && (source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) source).getUniqueId()).isPresent() && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId()).get().getName().equals(faction.getName())))
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION));
                    }
                    else if((!source.hasPermission(PluginPermissions.INFO_COMMAND) && !source.hasPermission(PluginPermissions.INFO_COMMAND_OTHERS)) && (source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player) source).getUniqueId()).isPresent() && !getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId()).get().getName().equals(faction.getName())))
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_OTHER_FACTIONS));
                    }
                    else
                    {
                        showFactionInfo(source, faction);
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PERMISSIONS_TO_USE_THIS_COMMAND));
                }
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
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f info <faction name>"));
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
        			.filter(optionalName -> optionalName.isPresent()).map(optionalName -> optionalName.get())
        			.collect(Collectors.joining(", "));
        }

        String membersList = "";
        if(!faction.getMembers().isEmpty())
        {
        	membersList = faction.getMembers().stream()
        			.map(member -> getPlugin().getPlayerManager().getPlayerName(member))
        			.filter(optionalName -> optionalName.isPresent()).map(optionalName -> optionalName.get())
        			.collect(Collectors.joining(", "));
        }

        String officersList = "";
        if(!faction.getOfficers().isEmpty()) {
        	officersList = faction.getOfficers().stream()
        			.map(officer -> getPlugin().getPlayerManager().getPlayerName(officer))
        			.filter(optionalName -> optionalName.isPresent()).map(optionalName -> optionalName.get())
        			.collect(Collectors.joining(", "));		
        }		 

        String alliancesList = "";
        if(!faction.getAlliances().isEmpty())
        {
        	alliancesList = faction.getAlliances().stream().collect(Collectors.joining(", "));
        }

        String enemiesList = "";
        if(!faction.getEnemies().isEmpty())
        {
        	enemiesList = faction.getEnemies().stream().collect(Collectors.joining(", "));
        }


        Text info = Text.builder()
                .append(Text.of(TextColors.AQUA, PluginMessages.NAME + ": ", TextColors.GOLD, faction.getName() + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.TAG + ": "), faction.getTag().toBuilder().color(TextColors.GOLD).build(), Text.of("\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.LAST_ONLINE + ": "), lastOnline(faction), Text.of("\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.DESCRIPTION + ": ", TextColors.GOLD, faction.getDescription() + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.MOTD + ": ", TextColors.GOLD, faction.getMessageOfTheDay() + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.PUBLIC + ": ", TextColors.GOLD, faction.isPublic() + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.LEADER + ": ", TextColors.GOLD, leaderName + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.OFFICERS + ": ", TextColors.GOLD, officersList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.ALLIANCES + ": ", TextColors.BLUE, alliancesList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.ENEMIES + ": ", TextColors.RED, enemiesList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.MEMBERS + ": ", TextColors.GREEN, membersList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.RECRUITS + ": ", TextColors.GREEN, recruitList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.POWER + ": ", TextColors.GOLD, super.getPlugin().getPowerManager().getFactionPower(faction) + "/" + super.getPlugin().getPowerManager().getFactionMaxPower(faction) + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.CLAIMS + ": ", TextColors.GOLD, faction.getClaims().size() + "/" + super.getPlugin().getPowerManager().getFactionMaxClaims(faction)))
                .build();

        factionInfo.add(info);

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, PluginMessages.FACTION_INFO)).contents(factionInfo);
        paginationBuilder.sendTo(source);
    }

    private Text lastOnline(final Faction faction)
    {
        if(getPlugin().getFactionLogic().hasOnlinePlayers(faction))
            return Text.of(TextColors.GREEN, PluginMessages.NOW);

        final Date date = Date.from(faction.getLastOnline());
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        final String formattedDate = formatter.format(date);
        return Text.of(TextColors.RED, formattedDate);
    }
}