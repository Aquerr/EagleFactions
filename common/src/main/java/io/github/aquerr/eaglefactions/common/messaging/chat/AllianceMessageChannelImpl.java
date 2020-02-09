package io.github.aquerr.eaglefactions.common.messaging.chat;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.chat.AllianceMessageChannel;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllianceMessageChannelImpl extends AbstractMutableMessageChannel implements AllianceMessageChannel
{
	private final Set<Faction> factions;

	public AllianceMessageChannelImpl(final Faction faction)
	{
		super();
		final FactionLogic factionLogic = EagleFactionsPlugin.getPlugin().getFactionLogic();
		final Set<Faction> allianceFactions = new HashSet<>();
		allianceFactions.add(faction);
		for(final String allianceFactionName : faction.getAlliances())
		{
			final Faction allianceFaction = factionLogic.getFactionByName(allianceFactionName);
			if(allianceFactionName == null)
				continue;
			allianceFactions.add(allianceFaction);
		}
		factions = Collections.unmodifiableSet(allianceFactions);
	}

	public AllianceMessageChannelImpl(final Set<Faction> factions)
	{
		super();
		this.factions = Collections.unmodifiableSet(factions);
		getReceivers();
	}

	private void getReceivers()
	{
		for(final Faction faction : this.factions)
		{
			final List<Player> players = EagleFactionsPlugin.getPlugin().getFactionLogic().getOnlinePlayers(faction);
			for(final Player player : players)
			{
				super.addMember(player);
			}
		}
	}

	@Override
	public Set<Faction> getFactions()
	{
		return this.factions;
	}
}
