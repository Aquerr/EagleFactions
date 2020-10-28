package io.github.aquerr.eaglefactions.common.commands.rank;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
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
import java.util.UUID;

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

        if(!(source instanceof Player))
            return promoteByConsole(source, promotedPlayer);

        final Player sourcePlayer = (Player)source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(sourcePlayer.getUniqueId());
        final Optional<Faction> optionalPromotedPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(promotedPlayer.getUniqueId());

        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        if(!optionalPromotedPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if(!optionalPromotedPlayerFaction.get().getName().equals(playerFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if(super.getPlugin().getPlayerManager().hasAdminMode(sourcePlayer))
        {
            if (promotedPlayer.getUniqueId().equals(playerFaction.getLeader()) || playerFaction.getOfficers().contains(promotedPlayer.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
            return promotePlayer(sourcePlayer, promotedPlayer, playerFaction);
        }

        if(playerFaction.getLeader().equals(sourcePlayer.getUniqueId()))
        {
            if (promotedPlayer.getUniqueId().equals(playerFaction.getLeader()) || playerFaction.getOfficers().contains(promotedPlayer.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
            return promotePlayer(sourcePlayer, promotedPlayer, playerFaction);
        }
        else if(playerFaction.getOfficers().contains(sourcePlayer.getUniqueId()))
        {
            if (playerFaction.getLeader().equals(promotedPlayer.getUniqueId()) || playerFaction.getOfficers().contains(promotedPlayer.getUniqueId()) || playerFaction.getMembers().contains(promotedPlayer.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));
            return promotePlayer(sourcePlayer, promotedPlayer, playerFaction);
        }
        else
        {
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
        }
    }

    private CommandResult promoteByConsole(final CommandSource source, final FactionPlayer promotedPlayer) throws CommandException
    {
        final Faction faction = promotedPlayer.getFaction().orElseThrow(() -> new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "This player is not in a faction.")));
        if (faction.getOfficers().contains(promotedPlayer.getUniqueId()))
        {
            super.getPlugin().getFactionLogic().setLeader(promotedPlayer.getUniqueId(), faction.getName());
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, promotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, Messages.LEADER)))));
            return CommandResult.success();
        }

        if (faction.getOfficers().contains(promotedPlayer.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_PROMOTE_THIS_PLAYER_MORE));

        final FactionMemberType promotedTo = super.getPlugin().getFactionLogic().promotePlayer(faction, promotedPlayer.getUniqueId());
        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, promotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, promotedTo.name())))));
        return CommandResult.success();
    }

    private CommandResult promotePlayer(final Player promotedBy, final FactionPlayer promotedPlayer, final Faction faction)
    {
        final boolean isCancelled = EventRunner.runFactionPromoteEventPre(promotedBy, promotedPlayer, faction);
        if (isCancelled)
            return CommandResult.success();

        final FactionMemberType promotedTo = getPlugin().getFactionLogic().promotePlayer(faction, promotedPlayer.getUniqueId());
        promotedBy.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, promotedPlayer.getName()), Placeholders.MEMBER_TYPE, Text.of(TextColors.GOLD, promotedTo.name())))));

        EventRunner.runFactionPromoteEventPost(promotedBy, promotedPlayer, promotedTo, faction);
        return CommandResult.success();
    }
}
