package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.Invite;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
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

public class JoinCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public JoinCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Faction faction = context.requireOne("faction");

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        if (super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId()).isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ARE_ALREADY_IN_A_FACTION));

        //If player has admin mode then force join.
        if(super.getPlugin().getPlayerManager().hasAdminMode(player))
        {
            return joinFactionAndNotify(player, faction);
        }

        if(!faction.isPublic())
        {
            boolean hasInvite = false;
            for (final Invite invite: EagleFactionsPlugin.INVITE_LIST)
            {
                if(invite.getPlayerUUID().equals(player.getUniqueId()) && invite.getFactionName().equals(faction.getName()))
                {
                    hasInvite = true;
                }
            }
            if(!hasInvite)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_HAVENT_BEEN_INVITED_TO_THIS_FACTION));
        }

        //TODO: Should public factions bypass this restriction?
        if(this.factionsConfig.isPlayerLimit())
        {
            int playerCount = 0;
            playerCount += faction.getLeader().toString().equals("") ? 0 : 1;
            playerCount += faction.getOfficers().isEmpty() ? 0 : faction.getOfficers().size();
            playerCount += faction.getMembers().isEmpty() ? 0 : faction.getMembers().size();
            playerCount += faction.getRecruits().isEmpty() ? 0 : faction.getRecruits().size();

            if(playerCount >= this.factionsConfig.getPlayerLimit())
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_JOIN_THIS_FACTION_BECAUSE_IT_REACHED_ITS_PLAYER_LIMIT));
        }

        return joinFactionAndNotify(player, faction);
    }

    private CommandResult joinFactionAndNotify(final Player player, final Faction faction)
    {
        final boolean isCancelled = EventRunner.runFactionJoinEvent(player, faction);
        if (isCancelled)
            return CommandResult.success();

        super.getPlugin().getFactionLogic().joinFaction(player.getUniqueId(), faction.getName());
        EagleFactionsPlugin.INVITE_LIST.remove(new Invite(faction.getName(), player.getUniqueId()));
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.SUCCESSFULLY_JOINED_FACTION, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));
        return CommandResult.success();
    }
}
