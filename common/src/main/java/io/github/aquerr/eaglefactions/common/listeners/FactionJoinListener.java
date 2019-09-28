package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;

import java.util.List;

public class FactionJoinListener extends AbstractListener
{
	public FactionJoinListener(final EagleFactions plugin)
	{
		super(plugin);
	}

	@Listener(order = Order.POST)
	@IsCancelled(value = Tristate.FALSE)
	public void onFactionJoin(final FactionLeaveEvent event, @Root final Player player)
	{
		//Notify other faction members about someone joining the faction.
		final Faction faction = event.getFaction();
		final List<Player> factionPlayers = super.getPlugin().getFactionLogic().getOnlinePlayers(faction);
		for (final Player factionPlayer : factionPlayers)
		{
			if (factionPlayer.getName().equals(player.getName()))
				continue;
			factionPlayer.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, player.getName(), TextColors.AQUA, " joined your faction."));
		}
	}
}
