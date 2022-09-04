package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class InviteCommand extends AbstractCommand
{
    private final FactionLogic factionLogic;
    private final PermsManager permsManager;
    private final FactionsConfig factionsConfig;
    private final InvitationManager invitationManager;
    private final MessageService messageService;

    public InviteCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.invitationManager = plugin.getInvitationManager();
        this.permsManager = plugin.getPermsManager();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer invitedPlayer = context.requireOne(CommonParameters.PLAYER);
        final ServerPlayer senderPlayer = requirePlayerSource(context);
        final Faction senderFaction = requirePlayerFaction(senderPlayer);
        if (!this.permsManager.canInvite(senderPlayer.uniqueId(), senderFaction))
            throw messageService.resolveExceptionWithMessage("error.command.invite.players-with-your-rank-cant-invite-players-to-faction");

        if(hasReachedPlayerLimit(senderFaction))
            throw messageService.resolveExceptionWithMessage("error.command.invite.you-cant-invite-more-players-to-faction-limit-reached");

        if(this.factionLogic.getFactionByPlayerUUID(invitedPlayer.uniqueId()).isPresent())
            throw messageService.resolveExceptionWithMessage("error.command.invite.this-player-is-already-in-a-faction");

        this.invitationManager.sendInvitation(senderPlayer, invitedPlayer, senderFaction);
        return CommandResult.success();
    }

    private boolean hasReachedPlayerLimit(Faction faction)
    {
        return this.factionsConfig.isPlayerLimit() && faction.getPlayers().size() >= this.factionsConfig.getPlayerLimit();
    }
}
