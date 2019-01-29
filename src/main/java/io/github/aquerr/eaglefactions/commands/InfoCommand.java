package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
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

/**
 * Created by Aquerr on 2017-08-03.
 */
public class InfoCommand extends AbstractCommand
{
    public InfoCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne("faction name");

        if (optionalFactionName.isPresent())
        {
            String rawFactionName = optionalFactionName.get();
            Faction faction = getPlugin().getFactionLogic().getFactionByName(rawFactionName);

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
        else if(source instanceof Player && getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId()).isPresent())
        {
            //Check permissions
            if(source.hasPermission(PluginPermissions.INFO_COMMAND) || source.hasPermission(PluginPermissions.INFO_COMMAND_SELF))
            {
                showFactionInfo(source, getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId()).get());
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

    private void showFactionInfo(CommandSource source, Faction faction)
    {
        List<Text> factionInfo = new ArrayList<>();

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
            for (UUID recruit : faction.getRecruits())
            {
                Optional<String> optionalName = getPlugin().getPlayerManager().getPlayerName(recruit);
                if(optionalName.isPresent())
                    recruitList += optionalName.get() + ", ";
            }
            if(recruitList.length() > 2)
                recruitList = recruitList.substring(0, recruitList.length() - 2);
        }

        String membersList = "";
        if(!faction.getMembers().isEmpty())
        {
            for (UUID member: faction.getMembers())
            {
                Optional<String> optionalName = getPlugin().getPlayerManager().getPlayerName(member);
                if(optionalName.isPresent())
                    membersList += optionalName.get() + ", ";
            }
            if(membersList.length() > 2)
                membersList = membersList.substring(0, membersList.length() - 2);
        }

        String officersList = "";
        if(!faction.getOfficers().isEmpty())
        {
            for (UUID officer: faction.getOfficers())
            {
                Optional<String> optionalName = getPlugin().getPlayerManager().getPlayerName(officer);
                if(optionalName.isPresent())
                    officersList += optionalName.get() + ", ";
            }
            if(officersList.length() > 2)
                officersList = officersList.substring(0, officersList.length() - 2);
        }

        String alliancesList = "";
        if(!faction.getAlliances().isEmpty())
        {
            for (String alliance: faction.getAlliances())
            {
                alliancesList += alliance + ", ";
            }
            alliancesList = alliancesList.substring(0, alliancesList.length() - 2);
        }

        String enemiesList = "";
        if(!faction.getEnemies().isEmpty())
        {
            for (String enemy: faction.getEnemies())
            {
                enemiesList += enemy + ", ";
            }
            enemiesList = enemiesList.substring(0, enemiesList.length() - 2);
        }


        Text info = Text.builder()
                .append(Text.of(TextColors.AQUA, PluginMessages.NAME + ": ", TextColors.GOLD, faction.getName() + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.TAG + ": "), faction.getTag().toBuilder().color(TextColors.GOLD).build(), Text.of("\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.LAST_ONLINE + ": "), lastOnline(faction), Text.of("\n"))
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

    private Text lastOnline(Faction faction)
    {
        if(getPlugin().getFactionLogic().hasOnlinePlayers(faction))
            return Text.of(TextColors.GREEN, PluginMessages.NOW);

        Date date = Date.from(faction.getLastOnline());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = formatter.format(date);
        return Text.of(TextColors.RED, formattedDate);
    }
}
