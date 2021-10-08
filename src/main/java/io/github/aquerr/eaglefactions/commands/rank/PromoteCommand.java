package io.github.aquerr.eaglefactions.commands.rank;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.exception.PlayerNotInFactionException;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
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
public class PromoteCommand extends AbstractCommand
{
    public PromoteCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final FactionPlayer promotedPlayer = context.requireOne("player");

        if(!isPlayer(source))
            return promoteByConsole(source, promotedPlayer);

        final Player sourcePlayer = requirePlayerSource(source);
        final Faction playerFaction = requirePlayerFaction(sourcePlayer);
        promotedPlayer.getFaction()
                .filter(faction -> faction.getName().equals(playerFaction.getName()))
                .orElseThrow(() -> new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION)));

        return tryPromotePlayer(playerFaction, sourcePlayer, promotedPlayer);
    }

    private CommandResult tryPromotePlayer(final Faction faction, final Player sourcePlayer, final FactionPlayer targetPlayer) throws CommandException
    {
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer);
        final FactionMemberType sourcePlayerMemberType = faction.getPlayerMemberType(sourcePlayer.getUniqueId());
        final FactionMemberType targetPlayerMemberType = targetPlayer.getFactionRole();

        if (hasAdminMode)
        {
            if (targetPlayerMemberType == FactionMemberType.OFFICER)
            {
                super.getPlugin().getRankManager().setLeader(targetPlayer, faction);
                sourcePlayer.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, targetPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, Messages.LEADER)))));
                return CommandResult.success();
            }

            if (targetPlayerMemberType == FactionMemberType.LEADER)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));

            return promotePlayer(sourcePlayer,targetPlayer);
        }

        List<FactionMemberType> promotableRoles = getPromotableRolesForRole(sourcePlayerMemberType);
        if (!promotableRoles.contains(targetPlayerMemberType))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));

        return promotePlayer(sourcePlayer, targetPlayer);
    }

    private CommandResult promoteByConsole(final CommandSource source, final FactionPlayer promotedPlayer) throws CommandException
    {
        final Faction faction = promotedPlayer.getFaction().orElseThrow(() -> new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "This player is not in a faction.")));
        FactionMemberType targetPlayerRole = promotedPlayer.getFactionRole();
        if (targetPlayerRole == FactionMemberType.OFFICER)
        {
            super.getPlugin().getRankManager().setLeader(promotedPlayer, faction);
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, promotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, Messages.LEADER)))));
            return CommandResult.success();
        }

        if (targetPlayerRole == FactionMemberType.LEADER)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));

        final FactionMemberType promotedTo;
        try
        {
            promotedTo = super.getPlugin().getRankManager().promotePlayer(null, promotedPlayer);
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, promotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, promotedTo.name())))));
        }
        catch (PlayerNotInFactionException ignored)
        {
        }
        return CommandResult.success();
    }

    private CommandResult promotePlayer(final Player promotedBy, final FactionPlayer promotedPlayer)
    {
        final FactionMemberType promotedTo;
        try
        {
            promotedTo = getPlugin().getRankManager().promotePlayer(promotedBy, promotedPlayer);
            promotedBy.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, promotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, promotedTo.name())))));
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
