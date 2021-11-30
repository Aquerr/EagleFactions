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
import org.spongepowered.api.text.format.TextColors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
                leaderNameText = buildClickablePlayerNickname(optionalFactionPlayer.get());
            }
        }

        Text recruitList = Text.EMPTY;
        if(!faction.getRecruits().isEmpty())
        {
            recruitList = buildPlayerList(faction.getRecruits());
        }

        Text membersList = Text.EMPTY;
        if(!faction.getMembers().isEmpty())
        {
        	membersList = buildPlayerList(faction.getMembers());
        }

        Text officersList = Text.EMPTY;
        if(!faction.getOfficers().isEmpty())
        {
        	officersList = buildPlayerList(faction.getOfficers());
        }

        Text trucesList = Text.EMPTY;
        if(!faction.getTruces().isEmpty())
        {
            trucesList = buildRelationList(faction.getTruces());
        }

        Text alliancesList = Text.EMPTY;
        if(!faction.getAlliances().isEmpty())
        {
        	alliancesList = buildRelationList(faction.getAlliances());
        }

        Text enemiesList = Text.EMPTY;
        if(!faction.getEnemies().isEmpty())
        {
        	enemiesList = buildRelationList(faction.getEnemies());
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
                .append(Text.of(TextColors.AQUA, Messages.TRUCES + ": ", TextColors.GOLD, trucesList, Text.of("\n")))
                .append(Text.of(TextColors.AQUA, Messages.ALLIANCES + ": ", TextColors.BLUE, alliancesList, Text.of("\n")))
                .append(Text.of(TextColors.AQUA, Messages.ENEMIES + ": ", TextColors.RED, enemiesList, Text.of("\n")))
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

    private Text buildPlayerList(Collection<UUID> playerUUIDs)
    {
        Text playerList = Text.EMPTY;
        playerList = playerUUIDs.stream()
                .map(recruit -> getPlugin().getPlayerManager().getFactionPlayer(recruit))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::buildClickablePlayerNickname)
                .reduce(playerList, (text, text2) -> text.concat(text2).concat(Text.of(", ")));
        playerList = playerList.toBuilder().removeLastChild().build();
        return playerList;
    }

    private Text buildRelationList(Set<String> relations)
    {
        Text relationList = Text.EMPTY;
        relationList = relations.stream()
                .map(this::buildClickableFactionName)
                .reduce(relationList, ((text, text2) -> text.concat(text2).concat(Text.of(", "))));
        relationList = relationList.toBuilder().removeLastChild().build();
        return relationList;
    }

    private Text buildClickableFactionName(String factionName)
    {
        return Text.builder(factionName)
                .onHover(TextActions.showText(Text.of(TextColors.BLUE, "Click to view information about the faction")))
                .onClick(TextActions.runCommand("/f info " + factionName))
                .build();
    }

    private Text buildClickablePlayerNickname(FactionPlayer factionPlayer)
    {
        return Text.builder(factionPlayer.getName())
                .onHover(TextActions.showText(Text.of(TextColors.BLUE, "Click to view information about the player")))
                .onClick(TextActions.runCommand("/f player " + factionPlayer.getName()))
                .build();
    }
}