package io.github.aquerr.eaglefactions.common.commands.rank;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.exception.PlayerNotInFactionException;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

/**
 * Created by Aquerr on 2018-06-24.
 */
public class DemoteCommand extends AbstractCommand
{
    public DemoteCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final FactionPlayer demotedPlayer = context.requireOne("player");

        if(!isPlayer(source))
            return demoteByConsole(source, demotedPlayer);

        Player sourcePlayer = requirePlayerSource(source);
        final Faction playerFaction = requirePlayerFaction(sourcePlayer);
        super.getPlugin().getFactionLogic().getFactionByPlayerUUID(demotedPlayer.getUniqueId())
                .filter(faction -> faction.getName().equals(playerFaction.getName()))
                .orElseThrow(() -> new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION)));

        return tryDemotePlayer(playerFaction, sourcePlayer, demotedPlayer);
    }

    private CommandResult tryDemotePlayer(final Faction faction, final Player sourcePlayer, final FactionPlayer targetPlayer) throws CommandException
    {
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer);
        final FactionMemberType sourcePlayerMemberType = faction.getPlayerMemberType(sourcePlayer.getUniqueId());
        final FactionMemberType targetPlayerMemberType = targetPlayer.getFactionRole();

        if (hasAdminMode)
        {
            if (targetPlayerMemberType == FactionMemberType.RECRUIT)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));

            else if (targetPlayerMemberType == FactionMemberType.LEADER)
            {
                super.getPlugin().getRankManager().setLeader(null, faction);
                sourcePlayer.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, targetPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, Messages.OFFICER)))));
                return CommandResult.success();
            }

            return demotePlayer(sourcePlayer, targetPlayer);
        }

        List<FactionMemberType> demotableRoles = getDemotableRolesForRole(sourcePlayerMemberType);
        if (!demotableRoles.contains(targetPlayerMemberType))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));

        return demotePlayer(sourcePlayer, targetPlayer);
    }

    private CommandResult demoteByConsole(final CommandSource source, final FactionPlayer demotedPlayer) throws CommandException
    {
        final Faction faction = demotedPlayer.getFaction()
                .orElseThrow(() -> new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "This player is not in a faction.")));

        FactionMemberType targetPlayerRole = demotedPlayer.getFactionRole();

        if (targetPlayerRole == FactionMemberType.LEADER)
        {
            super.getPlugin().getRankManager().setLeader(null, faction);
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, demotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, Messages.OFFICER)))));
            return CommandResult.success();
        }

        if (targetPlayerRole == FactionMemberType.RECRUIT)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));

        FactionMemberType oldRank = demotedPlayer.getFactionRole();
        FactionMemberType demotedTo = null;
        try
        {
            demotedTo = super.getPlugin().getRankManager().demotePlayer(null, demotedPlayer);
            if (oldRank != demotedTo) {
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, demotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, demotedTo.name())))));
            }
        }
        catch (PlayerNotInFactionException ignored)
        {
        }
        return CommandResult.success();
    }

    private CommandResult demotePlayer(final Player demotedBy, final FactionPlayer demotedPlayer)
    {
        final FactionMemberType demotedTo;
        try
        {
            demotedTo = getPlugin().getRankManager().demotePlayer(demotedBy, demotedPlayer);
            demotedBy.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, demotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, demotedTo.name())))));
        }
        catch (PlayerNotInFactionException ignored)
        {
        }
        return CommandResult.success();
    }

    private List<FactionMemberType> getDemotableRolesForRole(FactionMemberType factionMemberType)
    {
        if (factionMemberType != FactionMemberType.LEADER && factionMemberType != FactionMemberType.OFFICER)
            return Collections.emptyList();

        //In case we want to add more roles in the future (probably, we will)
        List<FactionMemberType> roles = new ArrayList<>(Arrays.asList(FactionMemberType.values()));
        roles.remove(FactionMemberType.ALLY);
        roles.remove(FactionMemberType.TRUCE);
        roles.remove(FactionMemberType.RECRUIT);
        roles.remove(FactionMemberType.NONE);

        if (factionMemberType == FactionMemberType.LEADER)
        {
            roles.remove(FactionMemberType.LEADER);
        }
        else
        {
            roles.remove(FactionMemberType.LEADER);
            roles.remove(FactionMemberType.OFFICER);
        }
        return roles;
    }
}
