package io.github.aquerr.eaglefactions.messaging.chat;

import com.google.common.base.Preconditions;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.chat.FactionAudience;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class FactionAudienceImpl implements FactionAudience
{
	private final Faction faction;
	private final Set<Audience> audiences;

	public static FactionAudienceImpl forFaction(final Faction faction)
	{
		Preconditions.checkNotNull(faction);
		return new FactionAudienceImpl(faction);
	}

	public FactionAudienceImpl(final Faction faction)
	{
		super();
		this.audiences = new HashSet<>();
		this.faction = faction;
		registerReceivers();
	}

	@Override
	public Faction getFaction()
	{
		return this.faction;
	}

	private void registerReceivers()
	{
		final List<ServerPlayer> players = EagleFactionsPlugin.getPlugin().getFactionLogic().getOnlinePlayers(faction);
		for(final ServerPlayer player : players)
		{
			addAudience(player);
		}
		addAudience(Sponge.systemSubject());
		ChatMessageHelper.getAdminReceivers().forEach(this::addAudience);
	}

	@Override
	public @NotNull Iterable<? extends Audience> audiences()
	{
		return this.audiences;
	}

	private void addAudience(Audience audience)
	{
		this.audiences.add(audience);
	}
}
