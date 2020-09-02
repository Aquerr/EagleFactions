package io.github.aquerr.eaglefactions.common.messaging.chat;

import com.google.common.base.Preconditions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.chat.AllianceMessageChannel;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;

import java.util.*;
import java.util.stream.Collectors;

public class AllianceMessageChannelImpl extends AbstractMutableMessageChannel implements AllianceMessageChannel
{
	private final Set<Faction> factions;

	public static AllianceMessageChannelImpl forFaction(final Faction faction)
	{
		Preconditions.checkNotNull(faction);

		final FactionLogic factionLogic = EagleFactionsPlugin.getPlugin().getFactionLogic();
		final Set<Faction> receivers = new HashSet<>();
		receivers.add(faction);
		receivers.addAll(faction.getAlliances().stream().map(factionLogic::getFactionByName).filter(Objects::nonNull).collect(Collectors.toList()));

		return new AllianceMessageChannelImpl(receivers);
	}

	public static AllianceMessageChannelImpl forPlayer(final Player player)
	{
		Preconditions.checkNotNull(player);

		final Optional<Faction> optionalFaction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
		if (!optionalFaction.isPresent())
			throw new IllegalArgumentException("Player must belong to a faction!");
		return forFaction(optionalFaction.get());
	}

	public AllianceMessageChannelImpl(final Set<Faction> factions)
	{
		super();
		this.factions = Collections.unmodifiableSet(factions);
		getReceivers();
	}

	private void getReceivers()
	{
		// Don't really know if Sponge returns a copy of list with currently online players or the real list.
		// It would be best to create a new list to prevent any concurrent modifications while looping through the list.
		final Collection<Player> onlinePlayers = new ArrayList<>(Sponge.getServer().getOnlinePlayers());
		for (final Player player : onlinePlayers)
		{
			for (final Faction faction : this.factions)
			{
				if (faction.containsPlayer(player.getUniqueId()))
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
