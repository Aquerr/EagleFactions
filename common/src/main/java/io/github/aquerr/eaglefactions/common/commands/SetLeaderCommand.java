package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
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

import java.util.Collections;
import java.util.Optional;

public class SetLeaderCommand extends AbstractCommand
{
    public SetLeaderCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final FactionPlayer newLeaderPlayer = context.requireOne("player");

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        final Optional<Faction> optionalNewLeaderPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(newLeaderPlayer.getUniqueId());

        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        if(!optionalNewLeaderPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if(!optionalNewLeaderPlayerFaction.get().getName().equals(playerFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if (super.getPlugin().getPlayerManager().hasAdminMode(player))
        {
            if(playerFaction.getLeader().equals(newLeaderPlayer.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION));

            super.getPlugin().getFactionLogic().setLeader(newLeaderPlayer.getUniqueId(), playerFaction.getName());
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_SET_PLAYER_AS_YOUR_NEW_LEADER, Collections.singletonMap(Placeholders.PLAYER, Text.of(TextColors.GOLD, newLeaderPlayer.getName())))));
            return CommandResult.success();
        }
        else if (playerFaction.getLeader().equals(player.getUniqueId()))
        {
            if(playerFaction.getLeader().equals(newLeaderPlayer.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION));

            super.getPlugin().getFactionLogic().setLeader(newLeaderPlayer.getUniqueId(), playerFaction.getName());
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_SET_PLAYER_AS_YOUR_NEW_LEADER, Collections.singletonMap(Placeholders.PLAYER, Text.of(TextColors.GOLD, newLeaderPlayer.getName())))));
        }
        return CommandResult.success();
    }
}
