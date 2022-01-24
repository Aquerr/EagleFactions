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
public class PromoteCommand extends AbstractCommand
{
    public PromoteCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer promotedPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        if(!isServerPlayer(context.cause().audience()))
            return promoteByConsole(context.cause().audience(), promotedPlayer);

        final ServerPlayer sourcePlayer = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(sourcePlayer);
        promotedPlayer.getFaction()
                .filter(faction -> faction.getName().equals(playerFaction.getName()))
                .orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION, RED))));

        return tryPromotePlayer(playerFaction, sourcePlayer, promotedPlayer);
    }

    private CommandResult tryPromotePlayer(final Faction faction, final ServerPlayer sourcePlayer, final FactionPlayer targetPlayer) throws CommandException
    {
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer.user());
        final FactionMemberType sourcePlayerMemberType = faction.getPlayerMemberType(sourcePlayer.uniqueId());
        final FactionMemberType targetPlayerMemberType = targetPlayer.getFactionRole();

        if (hasAdminMode)
        {
            if (targetPlayerMemberType == FactionMemberType.OFFICER)
            {
                super.getPlugin().getRankManager().setLeader(targetPlayer, faction);
                sourcePlayer.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(targetPlayer.getName(), GOLD), Placeholders.MEMBER_TYPE, text(Messages.LEADER, GOLD)))));
                return CommandResult.success();
            }

            if (targetPlayerMemberType == FactionMemberType.LEADER)
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE, RED)));

            return promotePlayer(sourcePlayer,targetPlayer);
        }

        List<FactionMemberType> promotableRoles = getPromotableRolesForRole(sourcePlayerMemberType);
        if (!promotableRoles.contains(targetPlayerMemberType))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE, RED)));

        return promotePlayer(sourcePlayer, targetPlayer);
    }

    private CommandResult promoteByConsole(final Audience context, final FactionPlayer promotedPlayer) throws CommandException
    {
        final Faction faction = promotedPlayer.getFaction().orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text("This player is not in a faction.", RED))));
        FactionMemberType targetPlayerRole = promotedPlayer.getFactionRole();
        if (targetPlayerRole == FactionMemberType.OFFICER)
        {
            super.getPlugin().getRankManager().setLeader(promotedPlayer, faction);
            context.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(promotedPlayer.getName(), GOLD), Placeholders.MEMBER_TYPE, text(Messages.LEADER, GOLD)))));
            return CommandResult.success();
        }

        if (targetPlayerRole == FactionMemberType.LEADER)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE, RED)));

        final FactionMemberType promotedTo;
        try
        {
            promotedTo = super.getPlugin().getRankManager().promotePlayer(null, promotedPlayer);
            context.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(promotedPlayer.getName(), GOLD), Placeholders.MEMBER_TYPE, text(promotedTo.name(), GOLD)))));
        }
        catch (PlayerNotInFactionException ignored)
        {
        }
        return CommandResult.success();
    }

    private CommandResult promotePlayer(final ServerPlayer promotedBy, final FactionPlayer promotedPlayer)
    {
        final FactionMemberType promotedTo;
        try
        {
            promotedTo = getPlugin().getRankManager().promotePlayer(promotedBy, promotedPlayer);
            promotedBy.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(promotedPlayer.getName(), GOLD), Placeholders.MEMBER_TYPE, text(promotedTo.name(), GOLD)))));
        }
        catch (PlayerNotInFactionException ignored)
        {
        }

        return CommandResult.success();
    }

    private List<FactionMemberType> getPromotableRolesForRole(FactionMemberType factionMemberType)
    {
        if (factionMemberType != FactionMemberType.LEADER && factionMemberType != FactionMemberType.OFFICER)
            return Collections.emptyList();

        //In case we want to add more roles in the future (probably, we will)
        List<FactionMemberType> roles = new ArrayList<>(Arrays.asList(FactionMemberType.values()));
        roles.remove(FactionMemberType.ALLY);
        roles.remove(FactionMemberType.TRUCE);
        roles.remove(FactionMemberType.OFFICER);
        roles.remove(FactionMemberType.LEADER);
        roles.remove(FactionMemberType.NONE);

        if (factionMemberType == FactionMemberType.OFFICER)
        {
            roles.remove(FactionMemberType.MEMBER);
        }
        return roles;
    }
}
