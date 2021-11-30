package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InfoCommand extends AbstractCommand
{
    public InfoCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<Faction> faction = context.getOne("faction");
        if (faction.isPresent())
        {
            otherInfo(source, faction.get());
        }
        else
        {
            if (!(source instanceof Player))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

            final Faction playerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId())
                    .orElseThrow(() -> new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND)));

            selfInfo((Player)source, playerFaction);
        }
        return CommandResult.success();
    }

    private void selfInfo(final Player player, final Faction faction) throws CommandException
    {
        if (!player.hasPermission(PluginPermissions.INFO_COMMAND) && !player.hasPermission(PluginPermissions.INFO_COMMAND_SELF))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION));
        showFactionInfo(player, faction);
    }
    
    private void otherInfo(final CommandSource source, final Faction faction)
    {
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

    private void showFactionInfo(final CommandSource source, final Faction faction)
    {
        final List<Text> factionInfo = new ArrayList<>();

        Text leaderNameText = Text.EMPTY;
        if(faction.getLeader() != null && !faction.getLeader().equals(new UUID(0,0)))
        {
            final Optional<FactionPlayer> optionalFactionPlayer = super.getPlugin().getPlayerManager().getFactionPlayer(faction.getLeader());
            if (optionalFactionPlayer.isPresent())
            {
                leaderNameText = buildClickablePlayerNickname(TextColors.GOLD, optionalFactionPlayer.get());
            }
        }

        Text recruitList = Text.EMPTY;
        if(!faction.getRecruits().isEmpty())
        {
        	recruitList = faction.getRecruits().stream()
                    .map(recruit -> getPlugin().getPlayerManager().getFactionPlayer(recruit))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(factionPlayer -> buildClickablePlayerNickname(TextColors.GREEN, factionPlayer))
                    .reduce(recruitList, (text, text2) -> text.concat(text2).concat(Text.of(", ")));
            recruitList = recruitList.toBuilder().removeLastChild().build();
        }

        Text membersList = Text.EMPTY;
        if(!faction.getMembers().isEmpty())
        {
        	membersList = faction.getMembers().stream()
        			.map(member -> getPlugin().getPlayerManager().getFactionPlayer(member))
        			.filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(factionPlayer -> buildClickablePlayerNickname(TextColors.GREEN, factionPlayer))
                    .reduce(membersList, (text, text2) -> text.concat(text2).concat(Text.of(", ")));
            membersList = membersList.toBuilder().removeLastChild().build();
        }

        Text officersList = Text.EMPTY;
        if(!faction.getOfficers().isEmpty())
        {
        	officersList = faction.getOfficers().stream()
        			.map(officer -> getPlugin().getPlayerManager().getFactionPlayer(officer))
        			.filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(factionPlayer -> buildClickablePlayerNickname(TextColors.GOLD, factionPlayer))
                    .reduce(officersList, ((text, text2) -> text.concat(text2).concat(Text.of(", "))));
            officersList = officersList.toBuilder().removeLastChild().build();
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
                .append(Text.of(TextColors.AQUA, Messages.LEADER + ": ", TextColors.GOLD, leaderNameText, Text.of("\n")))
                .append(Text.of(TextColors.AQUA, Messages.OFFICERS + ": ", TextColors.GOLD, officersList, Text.of("\n")))
                .append(Text.of(TextColors.AQUA, Messages.TRUCES + ": ", TextColors.GOLD, trucesList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.ALLIANCES + ": ", TextColors.BLUE, alliancesList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.ENEMIES + ": ", TextColors.RED, enemiesList + "\n"))
                .append(Text.of(TextColors.AQUA, Messages.MEMBERS + ": ", TextColors.GREEN, membersList, Text.of("\n")))
                .append(Text.of(TextColors.AQUA, Messages.RECRUITS + ": ", TextColors.GREEN, recruitList, Text.of("\n")))
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
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String formattedDate = formatter.format(date);
        return Text.of(TextColors.RED, formattedDate);
    }

    private Text buildClickablePlayerNickname(TextColor textColor, FactionPlayer factionPlayer)
    {
        return Text.builder(factionPlayer.getName())
                .color(textColor)
                .onHover(TextActions.showText(Text.of(TextColors.BLUE, "Click to view information about the player")))
                .onClick(TextActions.runCommand("/f player " + factionPlayer.getName()))
                .build();
    }
}