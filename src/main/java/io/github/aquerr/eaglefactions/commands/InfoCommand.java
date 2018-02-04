package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PlayerService;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-03.
 */
public class InfoCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne("faction name");

        if (optionalFactionName.isPresent())
        {
            String rawFactionName = optionalFactionName.get();
            String factionName = FactionLogic.getRealFactionName(rawFactionName);

            if (factionName == null)
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no faction called ", TextColors.GOLD, rawFactionName + "!"));
            }
            else
            {
                if(source.hasPermission(PluginPermissions.InfoCommand) || source.hasPermission(PluginPermissions.InfoCommandSelf) || source.hasPermission(PluginPermissions.InfoCommandOthers))
                {
                    //Check permissions
                    if((!source.hasPermission(PluginPermissions.InfoCommand) && !source.hasPermission(PluginPermissions.InfoCommandSelf)) && (source instanceof Player && FactionLogic.getFactionName(((Player)source).getUniqueId()).equals(factionName)))
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have access to view information about your faction!"));
                    }
                    else if((!source.hasPermission(PluginPermissions.InfoCommand) && !source.hasPermission(PluginPermissions.InfoCommandOthers)) && (source instanceof Player && !FactionLogic.getFactionName(((Player)source).getUniqueId()).equals(factionName)))
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have access to view information about other factions!"));
                    }
                    else
                    {
                        showFactionInfo(source, factionName);
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have permissions to use this command!"));
                }
            }
        }
        else if(source instanceof Player && FactionLogic.getFactionName(((Player)source).getUniqueId()) != null)
        {
            //Check permissions
            if(source.hasPermission(PluginPermissions.InfoCommand) || source.hasPermission(PluginPermissions.InfoCommandSelf))
            {
                showFactionInfo(source, FactionLogic.getFactionName(((Player)source).getUniqueId()));
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have access to view information about your faction!"));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Wrong command arguments!"));
            source.sendMessage(Text.of(TextColors.RED, "Usage: /f info <faction name>"));
        }

        return CommandResult.success();
    }

    private void showFactionInfo(CommandSource source, String factionName)
    {
        Faction faction = FactionLogic.getFaction(factionName);

        List<Text> factionInfo = new ArrayList<>();

        String leaderName = "";
        if(faction.Leader != null && !faction.Leader.equals("")) leaderName = PlayerService.getPlayerName(UUID.fromString(faction.Leader)).get();

        String membersList = "";
        if(!faction.Members.isEmpty() && faction.Members != null)
        {
            for (String member: faction.Members)
            {
                membersList += PlayerService.getPlayerName(UUID.fromString(member)).get() + ", ";
            }
            membersList = membersList.substring(0, membersList.length() - 2);
        }

        String officersList = "";
        if(!faction.Officers.isEmpty() && faction.Officers != null)
        {
            for (String officer: faction.Officers)
            {
                officersList += PlayerService.getPlayerName(UUID.fromString(officer)).get() + ", ";
            }
            officersList = officersList.substring(0, officersList.length() - 2);
        }

        String alliancesList = "";
        if(!faction.Alliances.isEmpty() && faction.Alliances != null)
        {
            for (String alliance: faction.Alliances)
            {
                alliancesList += alliance + ", ";
            }
            alliancesList = alliancesList.substring(0, alliancesList.length() - 2);
        }

        String enemiesList = "";
        if(!faction.Enemies.isEmpty() && faction.Enemies != null)
        {
            for (String enemy: faction.Enemies)
            {
                enemiesList += enemy + ", ";
            }
            enemiesList = enemiesList.substring(0, enemiesList.length() - 2);
        }


        Text info = Text.builder()
                .append(Text.of(TextColors.AQUA, "Name: ", TextColors.GOLD, faction.Name + "\n"))
                .append(Text.of(TextColors.AQUA, "Tag: ", TextColors.GOLD, faction.Tag + "\n"))
                .append(Text.of(TextColors.AQUA, "Leader: ", TextColors.GOLD, leaderName + "\n"))
                .append(Text.of(TextColors.AQUA, "Officers: ", TextColors.GOLD, officersList + "\n"))
                .append(Text.of(TextColors.AQUA, "Alliances: ", TextColors.BLUE, alliancesList + "\n"))
                .append(Text.of(TextColors.AQUA, "Enemies: ", TextColors.RED, enemiesList + "\n"))
                .append(Text.of(TextColors.AQUA, "Members: ", TextColors.GREEN, membersList + "\n"))
                .append(Text.of(TextColors.AQUA, "Power: ", TextColors.GOLD, faction.Power + "/" + PowerService.getFactionMaxPower(faction) + "\n"))
                .append(Text.of(TextColors.AQUA, "Claims: ", TextColors.GOLD, String.valueOf(FactionLogic.getClaims(factionName).size()) + "/" + String.valueOf(faction.Power.intValue())))
                .build();

        factionInfo.add(info);

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "Faction Info")).contents(factionInfo);
        paginationBuilder.sendTo(source);
    }
}
