package io.github.aquerr.eaglefactions.commands.rank;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.exception.PlayerNotInFactionException;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

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
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer demotedPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        if(!isServerPlayer(context.cause().audience()))
            return demoteByConsole(context.cause().audience(), demotedPlayer);

        ServerPlayer sourcePlayer = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(sourcePlayer);
        super.getPlugin().getFactionLogic().getFactionByPlayerUUID(demotedPlayer.getUniqueId())
                .filter(faction -> faction.getName().equals(playerFaction.getName()))
                .orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION, RED))));

        return tryDemotePlayer(playerFaction, sourcePlayer, demotedPlayer);
    }

    private CommandResult tryDemotePlayer(final Faction faction, final ServerPlayer sourcePlayer, final FactionPlayer targetPlayer) throws CommandException
    {
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer.user());
        final FactionMemberType sourcePlayerMemberType = faction.getPlayerMemberType(sourcePlayer.uniqueId());
        final FactionMemberType targetPlayerMemberType = targetPlayer.getFactionRole();

        if (hasAdminMode)
        {
            if (targetPlayerMemberType == FactionMemberType.RECRUIT)
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE, RED)));

            else if (targetPlayerMemberType == FactionMemberType.LEADER)
            {
                super.getPlugin().getRankManager().setLeader(null, faction);
                sourcePlayer.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(targetPlayer.getName(), GOLD), Placeholders.MEMBER_TYPE, text(Messages.OFFICER, GOLD)))));
                return CommandResult.success();
            }

            return demotePlayer(sourcePlayer, targetPlayer);
        }

        List<FactionMemberType> demotableRoles = getDemotableRolesForRole(sourcePlayerMemberType);
        if (!demotableRoles.contains(targetPlayerMemberType))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE, RED)));

        return demotePlayer(sourcePlayer, targetPlayer);
    }

    private CommandResult demoteByConsole(final Audience audience, final FactionPlayer demotedPlayer) throws CommandException
    {
        final Faction faction = demotedPlayer.getFaction()
                .orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text("This player is not in a faction.", RED))));

        FactionMemberType targetPlayerRole = demotedPlayer.getFactionRole();

        if (targetPlayerRole == FactionMemberType.LEADER)
        {
            super.getPlugin().getRankManager().setLeader(null, faction);
            audience.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(demotedPlayer.getName(), GOLD), Placeholders.MEMBER_TYPE, text(Messages.OFFICER, GOLD)))));
            return CommandResult.success();
        }

        if (targetPlayerRole == FactionMemberType.RECRUIT)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE, RED)));

        FactionMemberType oldRank = demotedPlayer.getFactionRole();
        FactionMemberType demotedTo = null;
        try
        {
            demotedTo = super.getPlugin().getRankManager().demotePlayer(null, demotedPlayer);
            if (oldRank != demotedTo) {
                audience.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(demotedPlayer.getName(), GOLD), Placeholders.MEMBER_TYPE, text(demotedTo.name(), GOLD)))));
            }
        }
        catch (PlayerNotInFactionException ignored)
        {
        }
        return CommandResult.success();
    }

    private CommandResult demotePlayer(final ServerPlayer demotedBy, final FactionPlayer demotedPlayer)
    {
        final FactionMemberType demotedTo;
        try
        {
            demotedTo = getPlugin().getRankManager().demotePlayer(demotedBy, demotedPlayer);
            demotedBy.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(demotedPlayer.getName(), GOLD), Placeholders.MEMBER_TYPE, text(demotedTo.name(), GOLD)))));
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
