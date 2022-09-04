package io.github.aquerr.eaglefactions.commands.relation;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.AllyRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AllyCommand extends AbstractCommand
{
	private final InvitationManager invitationManager;
	private final MessageService messageService;

    public AllyCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.invitationManager = plugin.getInvitationManager();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Faction selectedFaction = context.requireOne(EagleFactionsCommandParameters.faction());
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
		AllyRequest allyRequest = findAllyRequest(selectedFaction, playerFaction);
		if (allyRequest != null) // Accept if request exists
		{
			allyRequest.accept();
			player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-have-accepted-invitation-from-faction", selectedFaction.getName()));
		}
		else // Invite if request does not exist
		{
			if (findAllyRequest(playerFaction, selectedFaction) != null)
				throw messageService.resolveExceptionWithMessage("error.relations.you-have-already-invited-this-faction-to-the-alliance");
			sendInvite(player, playerFaction, selectedFaction);
		}
        return CommandResult.success();
    }

    @Nullable
    private AllyRequest findAllyRequest(Faction senderFaction, Faction invitedFaction)
	{
		return EagleFactionsPlugin.RELATION_INVITES.stream()
				.filter(AllyRequest.class::isInstance)
				.map(AllyRequest.class::cast)
				.filter(allyRequest -> allyRequest.getInvitedFaction().equals(invitedFaction.getName()) && allyRequest.getSenderFaction().equals(senderFaction.getName()))
				.findFirst()
				.orElse(null);
	}

	private void sendInvite(final ServerPlayer player, final Faction playerFaction, final Faction targetFaction)
	{
		this.invitationManager.sendAllyRequest(player, playerFaction, targetFaction);
	}
}
