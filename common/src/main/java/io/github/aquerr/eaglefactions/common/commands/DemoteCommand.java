package io.github.aquerr.eaglefactions.common.commands;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.PluginInfo;
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

import java.util.Optional;

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

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player sourcePlayer = (Player)source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(sourcePlayer.getUniqueId());
        final Optional<Faction> optionalDemotedPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(demotedPlayer.getUniqueId());

        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        if(!optionalDemotedPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if(!optionalDemotedPlayerFaction.get().getName().equals(playerFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if(super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer))
        {
            if(!playerFaction.getLeader().equals(demotedPlayer.getUniqueId()) && !playerFaction.getOfficers().contains(demotedPlayer.getUniqueId()))
            {
                final FactionMemberType demotedTo = getPlugin().getFactionLogic().demotePlayer(playerFaction, demotedPlayer.getUniqueId());
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, demotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, demotedTo.name())))));
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));
            }
            return CommandResult.success();
        }

        if(playerFaction.getLeader().equals(sourcePlayer.getUniqueId()))
        {
            if(!playerFaction.getLeader().equals(demotedPlayer.getUniqueId()) && !playerFaction.getRecruits().contains(demotedPlayer.getUniqueId()))
            {
                final FactionMemberType demotedTo = super.getPlugin().getFactionLogic().demotePlayer(playerFaction, demotedPlayer.getUniqueId());
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, demotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, demotedTo.name())))));
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));
            }
        }
        else if(playerFaction.getOfficers().contains(sourcePlayer.getUniqueId()))
        {
            if(!playerFaction.getLeader().equals(demotedPlayer.getUniqueId()) && !playerFaction.getOfficers().contains(demotedPlayer.getUniqueId()) && !playerFaction.getMembers().contains(demotedPlayer.getUniqueId()))
            {
                final FactionMemberType demotedTo = getPlugin().getFactionLogic().demotePlayer(playerFaction, demotedPlayer.getUniqueId());
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, demotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, demotedTo.name())))));
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_DEMOTE_THIS_PLAYER_MORE));
            }
        }
        return CommandResult.success();
    }
}
