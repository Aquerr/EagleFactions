package io.github.aquerr.eaglefactions.commands.rank;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class SetLeaderCommand extends AbstractCommand
{
    public SetLeaderCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer newLeaderPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        final Faction newLeaderPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(newLeaderPlayer.getUniqueId()).orElse(null);

        if (newLeaderPlayerFaction == null || !newLeaderPlayerFaction.getName().equals(playerFaction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION, RED)));

        if (super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
        {
            if(playerFaction.getLeader().equals(newLeaderPlayer.getUniqueId()))
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION, RED)));

            super.getPlugin().getRankManager().setLeader(newLeaderPlayer, playerFaction);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_SET_PLAYER_AS_YOUR_NEW_LEADER, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(newLeaderPlayer.getName(), GOLD)))));
            return CommandResult.success();
        }
        else if (playerFaction.getLeader().equals(player.uniqueId()))
        {
            if(playerFaction.getLeader().equals(newLeaderPlayer.getUniqueId()))
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION, RED)));

            super.getPlugin().getRankManager().setLeader(newLeaderPlayer, playerFaction);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_SET_PLAYER_AS_YOUR_NEW_LEADER, GREEN, ImmutableMap.of(Placeholders.PLAYER, text(newLeaderPlayer.getName(), GOLD)))));
        }
        return CommandResult.success();
    }
}
