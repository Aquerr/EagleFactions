package io.github.aquerr.eaglefactions.commands.relation;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.TruceRequest;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class TruceCommand extends AbstractCommand
{
	private final InvitationManager invitationManager;
	private final MessageService messageService;

	public TruceCommand(final EagleFactions plugin)
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
		final TruceRequest truceRequest = findTruceRequest(selectedFaction, playerFaction);
		if (truceRequest != null)
		{
			try
			{
				truceRequest.accept();
			}
			catch (Exception exception)
			{
				throw new CommandException(Component.text(exception.getMessage()));

			}
			player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-have-accepted-invitation-from-faction", selectedFaction.getName()));
		}
		else
		{
			if (findTruceRequest(playerFaction, selectedFaction) != null)
				throw messageService.resolveExceptionWithMessage("error.relations.you-have-already-invited-this-faction-to-the-truce");
			sendInvite(player, playerFaction, selectedFaction);
		}
		return CommandResult.success();
	}

	@Nullable
	private TruceRequest findTruceRequest(Faction senderFaction, Faction invitedFaction)
	{
		return EagleFactionsPlugin.RELATION_INVITES.stream()
				.filter(TruceRequest.class::isInstance)
				.map(TruceRequest.class::cast)
				.filter(truceRequest -> truceRequest.getInvited().getName().equals(invitedFaction.getName()) && truceRequest.getSender().getName().equals(senderFaction.getName()))
				.findFirst()
				.orElse(null);
	}

	private void sendInvite(final ServerPlayer player, final Faction playerFaction, final Faction targetFaction)
	{
		this.invitationManager.sendTruceRequest(player, playerFaction, targetFaction);
	}
}
