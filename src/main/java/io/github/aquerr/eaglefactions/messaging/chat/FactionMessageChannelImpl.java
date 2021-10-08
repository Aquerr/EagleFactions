package io.github.aquerr.eaglefactions.messaging.chat;

import com.google.common.base.Preconditions;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.chat.FactionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FactionMessageChannelImpl extends AbstractMutableMessageChannel implements FactionMessageChannel
{
	private final Faction faction;

	public static FactionMessageChannelImpl forFaction(final Faction faction)
	{
		Preconditions.checkNotNull(faction);
		return new FactionMessageChannelImpl(faction);
	}

	public FactionMessageChannelImpl(final Faction faction)
	{
		super();
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
		final List<Player> players = EagleFactionsPlugin.getPlugin().getFactionLogic().getOnlinePlayers(faction);
		for(final Player player : players)
		{
			super.addMember(player);
		}
		super.addMember(Sponge.getServer().getConsole());
		getAdminReceivers().forEach(super::addMember);
	}

	private List<MessageReceiver> getAdminReceivers()
	{
		final List<MessageReceiver> admins = new ArrayList<>();
		for(final UUID adminUUID : EagleFactionsPlugin.getPlugin().getPlayerManager().getAdminModePlayers())
		{
			final Optional<Player> optionalAdminPlayer = Sponge.getServer().getPlayer(adminUUID);
			optionalAdminPlayer.ifPresent(admins::add);
		}
		return admins;
	}
}
