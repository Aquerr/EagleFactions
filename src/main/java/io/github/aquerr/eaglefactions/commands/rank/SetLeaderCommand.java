package io.github.aquerr.eaglefactions.commands.rank;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
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
        final Player player = requirePlayerSource(source);
        final Faction playerFaction = requirePlayerFaction(player);
        final Faction newLeaderPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(newLeaderPlayer.getUniqueId()).orElse(null);

        if (newLeaderPlayerFaction == null || !newLeaderPlayerFaction.getName().equals(playerFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));

        if (super.getPlugin().getPlayerManager().hasAdminMode(player))
        {
            if(playerFaction.getLeader().equals(newLeaderPlayer.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION));

            super.getPlugin().getRankManager().setLeader(newLeaderPlayer, playerFaction);
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_SET_PLAYER_AS_YOUR_NEW_LEADER, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, newLeaderPlayer.getName())))));
            return CommandResult.success();
        }
        else if (playerFaction.getLeader().equals(player.getUniqueId()))
        {
            if(playerFaction.getLeader().equals(newLeaderPlayer.getUniqueId()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION));

            super.getPlugin().getRankManager().setLeader(newLeaderPlayer, playerFaction);
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_SET_PLAYER_AS_YOUR_NEW_LEADER, TextColors.GREEN, ImmutableMap.of(Placeholders.PLAYER, Text.of(TextColors.GOLD, newLeaderPlayer.getName())))));
        }
        return CommandResult.success();
    }
}
