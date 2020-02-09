package io.github.aquerr.eaglefactions.common.messaging.chat;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.chat.AllianceMessageChannel;
import io.github.aquerr.eaglefactions.api.messaging.chat.FactionMessageChannel;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;

import java.util.List;

public class FactionMessageChannelImpl extends AbstractMutableMessageChannel implements FactionMessageChannel
{
	private final Faction faction;

	public FactionMessageChannelImpl(final Faction faction)
	{
		super();
		this.faction = faction;
		getReceivers();
	}

	@Override
	public Faction getFaction()
	{
		return this.faction;
	}

	private void getReceivers()
	{
		final List<Player> players = EagleFactionsPlugin.getPlugin().getFactionLogic().getOnlinePlayers(faction);
		for(final Player player : players)
		{
			super.addMember(player);
		}
	}
}
