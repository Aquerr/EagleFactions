package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
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
            Faction faction = FactionLogic.getFactionByName(rawFactionName);

            if (faction == null)
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, rawFactionName + "!"));
            }
            else
            {
                if(source.hasPermission(PluginPermissions.InfoCommand) || source.hasPermission(PluginPermissions.InfoCommandSelf) || source.hasPermission(PluginPermissions.InfoCommandOthers))
                {
                    //Check permissions
                    if((!source.hasPermission(PluginPermissions.InfoCommand) && !source.hasPermission(PluginPermissions.InfoCommandSelf)) && (source instanceof Player && FactionLogic.getFactionByPlayerUUID(((Player) source).getUniqueId()).isPresent() && FactionLogic.getFactionByPlayerUUID(((Player)source).getUniqueId()).get().Name.equals(faction.Name)))
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION));
                    }
                    else if((!source.hasPermission(PluginPermissions.InfoCommand) && !source.hasPermission(PluginPermissions.InfoCommandOthers)) && (source instanceof Player && FactionLogic.getFactionByPlayerUUID(((Player) source).getUniqueId()).isPresent() && !FactionLogic.getFactionByPlayerUUID(((Player)source).getUniqueId()).get().Name.equals(faction.Name)))
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_OTHER_FACTIONS));
                    }
                    else
                    {
                        showFactionInfo(source, faction);
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PERMISSIONS_TO_USE_THIS_COMMAND));
                }
            }
        }
        else if(source instanceof Player && FactionLogic.getFactionByPlayerUUID(((Player)source).getUniqueId()).isPresent())
        {
            //Check permissions
            if(source.hasPermission(PluginPermissions.InfoCommand) || source.hasPermission(PluginPermissions.InfoCommandSelf))
            {
                showFactionInfo(source, FactionLogic.getFactionByPlayerUUID(((Player)source).getUniqueId()).get());
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f info <faction name>"));
        }

        return CommandResult.success();
    }

    private void showFactionInfo(CommandSource source, Faction faction)
    {
        List<Text> factionInfo = new ArrayList<>();

        String leaderName = "";
        if(!faction.Leader.equals("")) leaderName = PlayerManager.getPlayerName(UUID.fromString(faction.Leader)).get();

        String membersList = "";
        if(!faction.Members.isEmpty())
        {
            for (String member: faction.Members)
            {
                membersList += PlayerManager.getPlayerName(UUID.fromString(member)).get() + ", ";
            }
            membersList = membersList.substring(0, membersList.length() - 2);
        }

        String officersList = "";
        if(!faction.Officers.isEmpty())
        {
            for (String officer: faction.Officers)
            {
                officersList += PlayerManager.getPlayerName(UUID.fromString(officer)).get() + ", ";
            }
            officersList = officersList.substring(0, officersList.length() - 2);
        }

        String alliancesList = "";
        if(!faction.Alliances.isEmpty())
        {
            for (String alliance: faction.Alliances)
            {
                alliancesList += alliance + ", ";
            }
            alliancesList = alliancesList.substring(0, alliancesList.length() - 2);
        }

        String enemiesList = "";
        if(!faction.Enemies.isEmpty())
        {
            for (String enemy: faction.Enemies)
            {
                enemiesList += enemy + ", ";
            }
            enemiesList = enemiesList.substring(0, enemiesList.length() - 2);
        }


        Text info = Text.builder()
                .append(Text.of(TextColors.AQUA, PluginMessages.NAME + ": ", TextColors.GOLD, faction.Name + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.TAG + ": "), faction.Tag.toBuilder().color(TextColors.GOLD).build(), Text.of("\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.LEADER + ": ", TextColors.GOLD, leaderName + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.OFFICERS + ": ", TextColors.GOLD, officersList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.ALLIANCES + ": ", TextColors.BLUE, alliancesList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.ENEMIES + ": ", TextColors.RED, enemiesList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.MEMBERS + ": ", TextColors.GREEN, membersList + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.POWER + ": ", TextColors.GOLD, faction.Power + "/" + PowerManager.getFactionMaxPower(faction) + "\n"))
                .append(Text.of(TextColors.AQUA, PluginMessages.CLAIMS + ": ", TextColors.GOLD, String.valueOf(faction.Claims.size()) + "/" + String.valueOf(faction.Power.intValue())))
                .build();

        factionInfo.add(info);

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, PluginMessages.FACTION_INFO)).contents(factionInfo);
        paginationBuilder.sendTo(source);
    }
}
