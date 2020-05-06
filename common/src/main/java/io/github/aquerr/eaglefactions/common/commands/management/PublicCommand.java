package io.github.aquerr.eaglefactions.common.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class PublicCommand extends AbstractCommand
{
	public PublicCommand(final EagleFactions plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
	{
		Optional<Faction> optionalFaction = context.getOne(Text.of("faction"));

		if(!(source instanceof Player))
		{
			if(!optionalFaction.isPresent())
				throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You must specify faction name!"));
			super.getPlugin().getFactionLogic().setIsPublic(optionalFaction.get(), !optionalFaction.get().isPublic());
			final String publicMessage = !optionalFaction.get().isPublic() ? "Faction is now public." : "Faction is no longer public.";
			source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, publicMessage));
			return CommandResult.success();
		}

		final Player player = (Player)source;
		if(!optionalFaction.isPresent())
			optionalFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

		if(!optionalFaction.isPresent())
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

		final Faction faction = optionalFaction.get();
		final String publicMessage = !faction.isPublic() ? "Faction is now public." : "Faction is no longer public.";

		if(super.getPlugin().getPlayerManager().hasAdminMode(player))
		{
			source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, publicMessage));
			super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
			return CommandResult.success();
		}

		if(!faction.getLeader().equals(player.getUniqueId()) && !faction.getOfficers().contains(player.getUniqueId()))
			throw new CommandException(Text.of(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS)));

		super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
		source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, publicMessage));
		return CommandResult.success();
	}
}
