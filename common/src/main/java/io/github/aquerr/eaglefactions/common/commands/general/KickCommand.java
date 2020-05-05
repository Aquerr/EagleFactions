package io.github.aquerr.eaglefactions.common.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
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

import java.util.Collections;
import java.util.Optional;

public class KickCommand extends AbstractCommand
{
    public KickCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final FactionPlayer selectedPlayer = context.<FactionPlayer>requireOne(Text.of("player"));

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();
        if(!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));

        final Optional<Faction> optionalSelectedPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(selectedPlayer.getUniqueId());
        if(!optionalSelectedPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if(!optionalSelectedPlayerFaction.get().getName().equals(playerFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if(playerFaction.getLeader().equals(selectedPlayer.getUniqueId()) || (playerFaction.getOfficers().contains(player.getUniqueId()) && playerFaction.getOfficers().contains(selectedPlayer.getUniqueId())))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_KICK_THIS_PLAYER));

        final boolean isCancelled = EventRunner.runFactionKickEvent(selectedPlayer, player, playerFaction);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().kickPlayer(selectedPlayer.getUniqueId(), playerFaction.getName());
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_KICKED_PLAYER_FROM_THE_FACTION, Collections.singletonMap(Placeholders.PLAYER, Text.of(TextColors.GOLD, selectedPlayer.getName())))));

            if(super.getPlugin().getPlayerManager().isPlayerOnline(selectedPlayer.getUniqueId()))
            {
                super.getPlugin().getPlayerManager().getPlayer(selectedPlayer.getUniqueId()).get().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.YOU_WERE_KICKED_FROM_THE_FACTION));
            }

            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(selectedPlayer.getUniqueId());
            EagleFactionsPlugin.CHAT_LIST.remove(selectedPlayer.getUniqueId());
        }
        return CommandResult.success();
    }
}
