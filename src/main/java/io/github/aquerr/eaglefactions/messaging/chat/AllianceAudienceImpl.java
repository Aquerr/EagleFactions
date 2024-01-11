package io.github.aquerr.eaglefactions.messaging.chat;

import com.google.common.base.Preconditions;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.chat.AllianceAudience;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.*;
import java.util.stream.Collectors;

public class AllianceAudienceImpl implements AllianceAudience
{
	private final Set<Faction> factions;
	private final Set<Audience> audiences;

	public static AllianceAudienceImpl forFaction(final Faction faction)
	{
		Preconditions.checkNotNull(faction);

		final FactionLogic factionLogic = EagleFactionsPlugin.getPlugin().getFactionLogic();
		final Set<Faction> receivers = new HashSet<>();
		receivers.add(faction);
		receivers.addAll(faction.getAlliances().stream().map(factionLogic::getFactionByName).filter(Objects::nonNull).collect(Collectors.toList()));

		return new AllianceAudienceImpl(receivers);
	}

	public AllianceAudienceImpl(final Set<Faction> factions)
	{
		super();
		this.audiences = new HashSet<>();
		this.factions = Collections.unmodifiableSet(factions);
		registerReceivers();
	}

	private void registerReceivers()
	{
		// Don't really know if Sponge returns a copy of list with currently online players or the real list.
		// It would be best to create a new list to prevent any concurrent modifications while looping through the list.
		final Collection<ServerPlayer> onlinePlayers = new ArrayList<>(Sponge.server().onlinePlayers());
		for (final ServerPlayer player : onlinePlayers)
		{
			for (final Faction faction : this.factions)
			{
				if (faction.containsPlayer(player.uniqueId()))
					addAudience(player);
			}
		}

		addAudience(Sponge.systemSubject());
		ChatMessageHelper.getAdminReceivers().forEach(this::addAudience);
	}

	@Override
	public Set<Faction> getFactions()
	{
		return this.factions;
	}

	private void addAudience(Audience audience)
	{
		this.audiences.add(audience);
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences()
	{
		return this.audiences;
	}
}
