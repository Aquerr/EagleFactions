package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.transaction.NotificationTicket;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Iterator;

public class NotifyNeighborBlockListener extends AbstractListener
{
	public NotifyNeighborBlockListener(final EagleFactions plugin)
	{
		super(plugin);
	}

	@Listener(order = Order.FIRST, beforeModifications = true)
	public void onNeighborNotify(final NotifyNeighborBlockEvent event)
	{
		final Cause cause = event.cause();
		final BlockEntity tileEntity = event.cause().first(BlockEntity.class).orElse(null);
		final LocatableBlock locatableBlock = cause.first(LocatableBlock.class).orElse(null);
		final ServerLocation tileEntityLocation = tileEntity != null ? tileEntity.serverLocation() : null;
		ServerLocation sourceLocation = locatableBlock != null ? locatableBlock.serverLocation() : tileEntityLocation;

		User user = getUserFromEvent(event).orElse(null);
		if(user == null)
		{
			if (sourceLocation == null)
				return;

			final Iterator<NotificationTicket> notificationTicketIterator = event.tickets().iterator();
			while(notificationTicketIterator.hasNext())
			{
				final NotificationTicket notificationTicket = notificationTicketIterator.next();
				final ServerLocation blockLocation = notificationTicket.target().location().orElse(null);
				if(!super.getPlugin().getProtectionManager().canNotifyBlock(sourceLocation, blockLocation).hasAccess())
				{
					notificationTicketIterator.remove();
				}
			}
			return;
		}

		if(sourceLocation == null)
		{
			final ServerPlayer player = event.cause().first(ServerPlayer.class).orElse(null);
			if(player == null)
				return;

			sourceLocation = player.serverLocation();
		}

		if(!super.getPlugin().getProtectionManager().canInteractWithBlock(sourceLocation, user, false).hasAccess())
		{
			event.setCancelled(true);
			return;
		}

		final Iterator<NotificationTicket> notificationTicketIterator = event.tickets().iterator();
		while(notificationTicketIterator.hasNext())
		{
			final NotificationTicket notificationTicket = notificationTicketIterator.next();
			final ServerLocation blockLocation = notificationTicket.target().location().orElse(null);
			if (blockLocation != null && (!super.getPlugin().getProtectionManager().canInteractWithBlock(blockLocation, user, false).hasAccess()))
				{
					notificationTicketIterator.remove();

			}
		}
	}
}
