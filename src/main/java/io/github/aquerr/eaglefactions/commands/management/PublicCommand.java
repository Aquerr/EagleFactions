package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class PublicCommand extends AbstractCommand
{
	private final MessageService messageService;

	public PublicCommand(final EagleFactions plugin)
	{
		super(plugin);
		this.messageService = plugin.getMessageService();
	}

	@Override
	public CommandResult execute(final CommandContext context) throws CommandException
	{
		Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.optionalFaction());

		if(!(isServerPlayer(context.cause().audience())))
		{
			if(!optionalFaction.isPresent())
				throw messageService.resolveExceptionWithMessage("error.command.public.no-faction-specified");
			final Faction faction = optionalFaction.get();
			final Component message = !faction.isPublic() ? messageService.resolveMessageWithPrefix("command.public.faction-is-now-public") : messageService.resolveMessageWithPrefix("command.public.faction-is-no-longer-public");
			super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
			context.sendMessage(Identity.nil(), message);
			return CommandResult.success();
		}

		final ServerPlayer player = requirePlayerSource(context);
		if(!optionalFaction.isPresent())
			optionalFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

		if(!optionalFaction.isPresent())
			throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY);

		final Faction faction = optionalFaction.get();
		final Component message = !faction.isPublic() ? messageService.resolveMessageWithPrefix("command.public.faction-is-now-public") : messageService.resolveMessageWithPrefix("command.public.faction-is-no-longer-public");

		if(super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
		{
			player.sendMessage(message);
			super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
			return CommandResult.success();
		}

		if(!faction.getLeader().equals(player.uniqueId()) && !faction.getOfficers().contains(player.uniqueId()))
			throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);

		super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
		player.sendMessage(message);
		return CommandResult.success();
	}
}
