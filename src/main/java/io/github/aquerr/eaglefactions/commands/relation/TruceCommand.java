package io.github.aquerr.eaglefactions.commands.relation;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.TruceRequest;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import javax.annotation.Nullable;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class TruceCommand extends AbstractCommand
{
	private final InvitationManager invitationManager;

	public TruceCommand(final EagleFactions plugin)
	{
		super(plugin);
		this.invitationManager = plugin.getInvitationManager();
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
			truceRequest.accept();
			player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_HAVE_ACCEPTED_AN_INVITATION_FROM_FACTION, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Component.text(selectedFaction.getName(), GOLD)))));
		}
		else
		{
			if (findTruceRequest(playerFaction, selectedFaction) != null)
				throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_HAVE_ALREADY_INVITED_THIS_FACTION_TO_THE_TRUCE, RED)));
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
				.filter(truceRequest -> truceRequest.getInvitedFaction().equals(invitedFaction.getName()) && truceRequest.getSenderFaction().equals(senderFaction.getName()))
				.findFirst()
				.orElse(null);
	}

	private void sendInvite(final ServerPlayer player, final Faction playerFaction, final Faction targetFaction)
	{
		this.invitationManager.sendTruceRequest(player, playerFaction, targetFaction);
	}
}
