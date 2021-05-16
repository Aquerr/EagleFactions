package io.github.aquerr.eaglefactions.common.commands.relation;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.AllyRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
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

import javax.annotation.Nullable;

public class AllyCommand extends AbstractCommand
{
	private final InvitationManager invitationManager;

    public AllyCommand(final EagleFactions plugin)
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
		AllyRequest allyRequest = findAllyRequest(selectedFaction, playerFaction);
		if (allyRequest != null) // Accept if request exists
		{
			allyRequest.accept();
			player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_HAVE_ACCEPTED_AN_INVITATION_FROM_FACTION, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, selectedFaction.getName())))));
		}
		else // Invite if request does not exist
		{
			if (findAllyRequest(playerFaction, selectedFaction) != null)
				throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_HAVE_ALREADY_INVITED_THIS_FACTION_TO_THE_ALLIANCE));
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

	private void sendInvite(final Player player, final Faction playerFaction, final Faction targetFaction)
	{
		this.invitationManager.sendAllyRequest(player, playerFaction, targetFaction);
	}
}
