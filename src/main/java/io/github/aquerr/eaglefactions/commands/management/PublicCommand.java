package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class PublicCommand extends AbstractCommand
{
	public PublicCommand(final EagleFactions plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(final CommandContext context) throws CommandException
	{
		Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());

		if(!(isPlayer(context)))
		{
			if(!optionalFaction.isPresent())
				throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text("You must specify faction name!", NamedTextColor.RED)));
			super.getPlugin().getFactionLogic().setIsPublic(optionalFaction.get(), !optionalFaction.get().isPublic());
			final String publicMessage = !optionalFaction.get().isPublic() ? "Faction is now public." : "Faction is no longer public.";
			context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text(publicMessage, NamedTextColor.GREEN)));
			return CommandResult.success();
		}

		final ServerPlayer player = requirePlayerSource(context);
		if(!optionalFaction.isPresent())
			optionalFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

		if(!optionalFaction.isPresent())
			throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

		final Faction faction = optionalFaction.get();
		final String publicMessage = !faction.isPublic() ? "Faction is now public." : "Faction is no longer public.";

		if(super.getPlugin().getPlayerManager().hasAdminMode(player))
		{
			player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(publicMessage, NamedTextColor.GREEN)));
			super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
			return CommandResult.success();
		}

		if(!faction.getLeader().equals(player.uniqueId()) && !faction.getOfficers().contains(player.uniqueId()))
			throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, NamedTextColor.RED)));

		super.getPlugin().getFactionLogic().setIsPublic(faction, !faction.isPublic());
		context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text(publicMessage, NamedTextColor.GREEN)));
		return CommandResult.success();
	}
}
