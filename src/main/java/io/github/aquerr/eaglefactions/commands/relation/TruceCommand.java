package io.github.aquerr.eaglefactions.commands.relation;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.TruceRequest;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
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

import javax.annotation.Nullable;

public class TruceCommand extends AbstractCommand
{
	private final InvitationManager invitationManager;

	public TruceCommand(final EagleFactions plugin)
	{
		super(plugin);
		this.invitationManager = plugin.getInvitationManager();
	}

	@Override
	public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
	{
		final Faction selectedFaction = context.requireOne(Text.of("faction"));
		final Player player = requirePlayerSource(source);
		final Faction playerFaction = requirePlayerFaction(player);
		final TruceRequest truceRequest = findTruceRequest(selectedFaction, playerFaction);
		if (truceRequest != null)
		{
			truceRequest.accept();
			player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_HAVE_ACCEPTED_AN_INVITATION_FROM_FACTION, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, selectedFaction.getName())))));
		}
		else
		{
			if (findTruceRequest(playerFaction, selectedFaction) != null)
				throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_HAVE_ALREADY_INVITED_THIS_FACTION_TO_THE_TRUCE));
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

	private void sendInvite(final Player player, final Faction playerFaction, final Faction targetFaction)
	{
		this.invitationManager.sendTruceRequest(player, playerFaction, targetFaction);
	}
}
