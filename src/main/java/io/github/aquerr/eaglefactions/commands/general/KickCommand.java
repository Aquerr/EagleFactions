package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class KickCommand extends AbstractCommand
{
    public KickCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer selectedPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        if(!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));

        final Optional<Faction> optionalSelectedPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(selectedPlayer.getUniqueId());
        if(!optionalSelectedPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION, RED)));

        if(!optionalSelectedPlayerFaction.get().getName().equals(playerFaction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION, RED)));

        if(playerFaction.getLeader().equals(selectedPlayer.getUniqueId()) || (playerFaction.getOfficers().contains(player.uniqueId()) && playerFaction.getOfficers().contains(selectedPlayer.getUniqueId())))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_KICK_THIS_PLAYER, RED)));

        final boolean isCancelled = EventRunner.runFactionKickEventPre(selectedPlayer, player, playerFaction);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().kickPlayer(selectedPlayer.getUniqueId(), playerFaction.getName());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_KICKED_PLAYER_FROM_THE_FACTION, GREEN, Collections.singletonMap(Placeholders.PLAYER, text(selectedPlayer.getName(), GOLD)))));

            if(super.getPlugin().getPlayerManager().isPlayerOnline(selectedPlayer.getUniqueId()))
            {
                super.getPlugin().getPlayerManager().getPlayer(selectedPlayer.getUniqueId()).ifPresent(p -> p.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.YOU_WERE_KICKED_FROM_THE_FACTION))));
            }

            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(selectedPlayer.getUniqueId());
            EagleFactionsPlugin.CHAT_LIST.remove(selectedPlayer.getUniqueId());
            EventRunner.runFactionKickEventPost(selectedPlayer, player, playerFaction);
        }

        return CommandResult.success();
    }
}
