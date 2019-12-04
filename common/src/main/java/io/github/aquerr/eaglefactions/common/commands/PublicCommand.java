package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
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
		final Optional<String> providedFactionName = context.getOne(Text.of("faction name"));

		if(!(source instanceof Player))
		{
			if(!providedFactionName.isPresent())
				throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You must specify the name of the faction!"));
			Optional<Faction> optionalFaction = Optional.ofNullable(super.getPlugin().getFactionLogic().getFactionByName(providedFactionName.get()));
			if(!optionalFaction.isPresent())
				throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Provided faction does not exist!"));
			super.getPlugin().getFactionLogic().setIsPublic(optionalFaction.get(), !optionalFaction.get().isPublic());
			final String publicMessage = !optionalFaction.get().isPublic() ? "Faction is now public." : "Faction is no longer public.";
			source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, publicMessage));
			return CommandResult.success();
		}

		final Player player = (Player)source;
		Optional<Faction> optionalFaction;
		if(providedFactionName.isPresent())
		{
			optionalFaction = Optional.ofNullable(super.getPlugin().getFactionLogic().getFactionByName(providedFactionName.get()));
		}
		else
		{
			optionalFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
		}

		if(!optionalFaction.isPresent())
			throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

		final Faction faction = optionalFaction.get();
		final String publicMessage = !faction.isPublic() ? "Faction is now public." : "Faction is no longer public.";

		if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
		{
			source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, publicMessage));
			super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
			return CommandResult.success();
		}

		if(!faction.getLeader().equals(player.getUniqueId()) && !faction.getOfficers().contains(player.getUniqueId()))
			throw new CommandException(Text.of(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS)));

		super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
		source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, publicMessage));
		return CommandResult.success();
	}
}
