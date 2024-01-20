package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class KickCommand extends AbstractCommand
{
    private final MessageService messageService;
    private final PermsManager permsManager;

    public KickCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final FactionPlayer selectedPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        if (!permsManager.hasPermission(player.uniqueId(), playerFaction, FactionPermission.KICK_MEMBERS))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);

        if(!playerFaction.containsPlayer(selectedPlayer.getUniqueId()))
            throw messageService.resolveExceptionWithMessage("error.general.this-player-is-not-in-your-faction");

        if(playerFaction.getLeader().getUniqueId().equals(selectedPlayer.getUniqueId())
                || hasSameOrHigherRank(playerFaction, player, selectedPlayer))
            throw messageService.resolveExceptionWithMessage("error.command.kick.you-cant-kick-this-player");

        final boolean isCancelled = EventRunner.runFactionKickEventPre(selectedPlayer, player, playerFaction);
        if(!isCancelled)
        {
            super.getPlugin().getFactionLogic().kickPlayer(selectedPlayer.getUniqueId(), playerFaction.getName());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.kick.you-kicked-player-from-the-faction"));

            if(super.getPlugin().getPlayerManager().isPlayerOnline(selectedPlayer.getUniqueId()))
            {
                super.getPlugin().getPlayerManager().getPlayer(selectedPlayer.getUniqueId())
                        .ifPresent(p -> p.sendMessage(messageService.resolveMessageWithPrefix("command.kick.you-were-kicked-from-the-faction")));
            }

            EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(selectedPlayer.getUniqueId());
            EagleFactionsPlugin.CHAT_LIST.remove(selectedPlayer.getUniqueId());
            EventRunner.runFactionKickEventPost(selectedPlayer, player, playerFaction);
        }

        return CommandResult.success();
    }

    private boolean hasSameOrHigherRank(Faction faction,
                                        ServerPlayer player,
                                        FactionPlayer selectedPlayer)
    {
        int selectedPlayerLadderPosition = RankManagerImpl.getHighestRank(selectedPlayer.getFactionRanks()).getLadderPosition();
        int sourcePlayerLadderPosition = RankManagerImpl.getHighestRank(faction.getPlayerRanks(player.uniqueId())).getLadderPosition();
        return selectedPlayerLadderPosition >= sourcePlayerLadderPosition;
    }
}
