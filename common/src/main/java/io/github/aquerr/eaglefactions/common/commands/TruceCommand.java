package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.AllyRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TruceCommand extends AbstractCommand
{
	public TruceCommand(final EagleFactions plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
	{
		final Faction selectedFaction = context.requireOne(Text.of("faction"));

		if(!(source instanceof Player))
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

		final Player player = (Player) source;
		final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

		if(!optionalPlayerFaction.isPresent())
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

		final Faction playerFaction = optionalPlayerFaction.get();
		if(playerFaction.getName().equals(selectedFaction.getName()))
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANNOT_INVITE_YOURSELF_TO_THE_TRUCE));

		if(super.getPlugin().getPlayerManager().hasAdminMode(player))
		{
			if(playerFaction.getEnemies().contains(selectedFaction.getName()))
				throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + Messages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));

			if(playerFaction.getAlliances().contains(selectedFaction.getName()))
			{
				super.getPlugin().getFactionLogic().removeAlly(playerFaction.getName(), selectedFaction.getName());
				//Add truce
				super.getPlugin().getFactionLogic().addTruce(playerFaction.getName(), selectedFaction.getName());
				player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.FACTION_HAS_BEEN_ADDED_TO_THE_TRUCE));
			}

			if(playerFaction.getTruces().contains(selectedFaction.getName()))
			{
				//Remove truce
				super.getPlugin().getFactionLogic().removeTruce(playerFaction.getName(), selectedFaction.getName());
				player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_DISBANDED_YOUR_TRUCE_WITH_FACTION, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, selectedFaction.getName())))));
			}
			else
			{
				//Add truce
				super.getPlugin().getFactionLogic().addTruce(playerFaction.getName(), selectedFaction.getName());
				player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.FACTION_HAS_BEEN_ADDED_TO_THE_TRUCE));
			}
			return CommandResult.success();
		}

		if(!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()))
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));

		if(playerFaction.getEnemies().contains(selectedFaction.getName()))
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + Messages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));

		if(playerFaction.getAlliances().contains(selectedFaction.getName()))
			throw new CommandException(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.DISBAND_ALLIANCE_FIRST_TO_INVITE_FACTION_TO_THE_TRUCE));

		if(playerFaction.getTruces().contains(selectedFaction.getName()))
		{
			//Remove truce
			super.getPlugin().getFactionLogic().removeTruce(playerFaction.getName(), selectedFaction.getName());
			player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_DISBANDED_YOUR_TRUCE_WITH_FACTION, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, selectedFaction.getName())))));
		}
		else
		{
			AllyRequest checkInvite = new AllyRequest(selectedFaction.getName(), playerFaction.getName());

			if(EagleFactionsPlugin.TRUCE_INVITE_LIST.contains(checkInvite))
			{
				acceptInvite(player, playerFaction, selectedFaction);
				EagleFactionsPlugin.TRUCE_INVITE_LIST.remove(checkInvite);
			}
			else if(!EagleFactionsPlugin.TRUCE_INVITE_LIST.contains(checkInvite))
			{
				sendInvite(player, playerFaction, selectedFaction);
			}
		}
		return CommandResult.success();
	}

	private void acceptInvite(final Player player, final Faction playerFaction, final Faction senderFaction)
	{
		super.getPlugin().getFactionLogic().addTruce(playerFaction.getName(), senderFaction.getName());
		player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_HAVE_ACCEPTED_AN_INVITATION_FROM_FACTION, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, senderFaction.getName())))));

		final Optional<Player> optionalSenderFactionLeader = super.getPlugin().getPlayerManager().getPlayer(senderFaction.getLeader());
		optionalSenderFactionLeader.ifPresent(x-> optionalSenderFactionLeader.get().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_TRUCE, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, playerFaction.getName()))))));
		senderFaction.getOfficers().forEach(x-> super.getPlugin().getPlayerManager().getPlayer(x).ifPresent(y-> Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_TRUCE, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, playerFaction.getName()))))));
	}

	private void sendInvite(final Player player, final Faction playerFaction, final Faction targetFaction) throws CommandException
	{
		final AllyRequest invite = new AllyRequest(playerFaction.getName(), targetFaction.getName());
		if(EagleFactionsPlugin.TRUCE_INVITE_LIST.contains(invite))
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, Messages.YOU_HAVE_ALREADY_INVITED_THIS_FACTION_TO_THE_TRUCE));

		EagleFactionsPlugin.TRUCE_INVITE_LIST.add(invite);

		final Optional<Player> optionalInvitedFactionLeader = super.getPlugin().getPlayerManager().getPlayer(targetFaction.getLeader());

		optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(getInviteGetMessage(playerFaction)));
		targetFaction.getOfficers().forEach(x-> super.getPlugin().getPlayerManager().getPlayer(x).ifPresent(y-> getInviteGetMessage(playerFaction)));

		player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, MessageLoader.parseMessage(Messages.YOU_HAVE_INVITED_FACTION_TO_THE_TRUCE, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));

		final Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
		taskBuilder.execute(() -> EagleFactionsPlugin.TRUCE_INVITE_LIST.remove(invite)).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(super.getPlugin());
	}

	private Text getInviteGetMessage(final Faction senderFaction)
	{
		final Text clickHereText = Text.builder()
				.append(Text.of(TextColors.AQUA, "[", TextColors.GOLD, Messages.CLICK_HERE, TextColors.AQUA, "]"))
				.onClick(TextActions.runCommand("/f truce " + senderFaction.getName()))
				.onHover(TextActions.showText(Text.of(TextColors.GOLD, "/f truce " + senderFaction.getName()))).build();

		return Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.FACTION_HAS_SENT_YOU_AN_INVITE_TO_THE_TRUCE,
				"\n", Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT,
				"\n", clickHereText, Messages.TO_ACCEPT_INVITATION_OR_TYPE, " ", TextColors.GOLD, "/f truce ", senderFaction.getName());
	}
}
