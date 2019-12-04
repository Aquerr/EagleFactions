package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.AllyRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AllyCommand extends AbstractCommand
{
    public AllyCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final String factionName = context.requireOne(Text.of("faction name"));

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        final Faction selectedFaction = getPlugin().getFactionLogic().getFactionByName(factionName);

        if(selectedFaction == null)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, factionName, TextColors.RED, "!"));

        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        if(playerFaction.getName().equals(selectedFaction.getName()))
        	throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Are you serious? You cannot invite yourself to the alliance!"));

        if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
        {
            if(playerFaction.getEnemies().contains(selectedFaction.getName()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + PluginMessages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));

			if(playerFaction.getAlliances().contains(selectedFaction.getName()))
			{
				//Remove ally
				super.getPlugin().getFactionLogic().removeAlly(playerFaction.getName(), selectedFaction.getName());
				player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, PluginMessages.YOU_DISBANDED_YOUR_ALLIANCE_WITH + " ", TextColors.GOLD, selectedFaction.getName(), TextColors.GREEN, "!"));
			}
			else
			{
				//Add ally
				super.getPlugin().getFactionLogic().addAlly(playerFaction.getName(), selectedFaction.getName());
				player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_ADDED_TO_THE_ALLIANCE));
			}
			return CommandResult.success();
        }

        if(!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));

        if(playerFaction.getEnemies().contains(selectedFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + PluginMessages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));

        if(playerFaction.getAlliances().contains(selectedFaction.getName()))
        {
            //Remove ally
            super.getPlugin().getFactionLogic().removeAlly(playerFaction.getName(), selectedFaction.getName());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, PluginMessages.YOU_DISBANDED_YOUR_ALLIANCE_WITH + " ", TextColors.GOLD, selectedFaction.getName(), TextColors.GREEN, "!"));
        }
        else
        {
            AllyRequest checkInvite = new AllyRequest(selectedFaction.getName(), playerFaction.getName());

            if(EagleFactionsPlugin.ALLY_INVITE_LIST.contains(checkInvite))
            {
            	acceptInvite(player, playerFaction, selectedFaction);
                EagleFactionsPlugin.ALLY_INVITE_LIST.remove(checkInvite);
            }
            else if(!EagleFactionsPlugin.ALLY_INVITE_LIST.contains(checkInvite))
            {
				sendInvite(player, playerFaction, selectedFaction);
            }
        }
        return CommandResult.success();
    }

    private void acceptInvite(final Player player, final Faction playerFaction, final Faction senderFaction)
	{
		super.getPlugin().getFactionLogic().addAlly(playerFaction.getName(), senderFaction.getName());
		player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.YOU_HAVE_ACCEPTED_AN_INVITATION_FROM + " ", TextColors.GOLD, senderFaction.getName() + "!"));

		final Optional<Player> optionalSenderFactionLeader = super.getPlugin().getPlayerManager().getPlayer(senderFaction.getLeader());
		optionalSenderFactionLeader.ifPresent(x-> optionalSenderFactionLeader.get().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.GREEN, " " + PluginMessages.ACCEPTED_YOUR_YOUR_INVITE_TO_THE_ALLIANCE)));
		senderFaction.getOfficers().forEach(x-> super.getPlugin().getPlayerManager().getPlayer(x).ifPresent(y-> Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.GREEN, " " + PluginMessages.ACCEPTED_YOUR_YOUR_INVITE_TO_THE_ALLIANCE)));
	}

	private void sendInvite(final Player player, final Faction playerFaction, final Faction targetFaction) throws CommandException
	{
		final AllyRequest invite = new AllyRequest(playerFaction.getName(), targetFaction.getName());
		if(EagleFactionsPlugin.ALLY_INVITE_LIST.contains(invite))
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, "You have already invited this factions to the alliance. Wait for their response!"));

		EagleFactionsPlugin.ALLY_INVITE_LIST.add(invite);

		final Optional<Player> optionalInvitedFactionLeader = super.getPlugin().getPlayerManager().getPlayer(targetFaction.getLeader());

		optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(getInviteGetMessage(playerFaction)));
		targetFaction.getOfficers().forEach(x-> super.getPlugin().getPlayerManager().getPlayer(x).ifPresent(y-> getInviteGetMessage(playerFaction)));

		player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.YOU_HAVE_INVITED_FACTION + " ", TextColors.GOLD, targetFaction.getName(), TextColors.GREEN, " " + PluginMessages.TO_THE_ALLIANCE));

		final Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
		taskBuilder.execute(() -> EagleFactionsPlugin.ALLY_INVITE_LIST.remove(invite)).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(super.getPlugin());
	}

	private Text getInviteGetMessage(final Faction senderFaction)
	{
		final Text clickHereText = Text.builder()
				.append(Text.of(TextColors.AQUA, "[", TextColors.GOLD, PluginMessages.CLICK_HERE, TextColors.AQUA, "]"))
				.onClick(TextActions.runCommand("/f ally " + senderFaction.getName()))
				.onHover(TextActions.showText(Text.of(TextColors.GOLD, "/f ally " + senderFaction.getName()))).build();

		return Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, senderFaction.getName(), TextColors.GREEN,
				" " + PluginMessages.HAS_SENT_YOU_AN_INVITE_TO_THE + " ", TextColors.AQUA, PluginMessages.ALLIANCE, TextColors.GREEN, "!" + "\n",
				PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT + "\n",
				clickHereText, TextColors.GREEN, " " + PluginMessages.TO_ACCEPT_INVITATION_OR_TYPE + " ", TextColors.GOLD, "/f ally " + senderFaction.getName());
	}
}
