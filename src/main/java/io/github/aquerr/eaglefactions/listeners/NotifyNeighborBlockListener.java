package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.transaction.NotificationTicket;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.*;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
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
		final EventContext context = event.context();
		final BlockEntity tileEntity = event.cause().first(BlockEntity.class).orElse(null);
		final LocatableBlock locatableBlock = cause.first(LocatableBlock.class).orElse(null);
		ServerLocation sourceLocation = locatableBlock != null ? locatableBlock.serverLocation() : tileEntity != null ? tileEntity.serverLocation() : null;

		User user;
//		if (cause.root() instanceof BlockEntity) {
//			user = context.get(EventContextKeys.OWNER)
//					.orElse(context.get(EventContextKeys.NOTIFIER)
//							.orElse(context.get(EventContextKeys.CREATOR)
//									.orElse(null)));
//		} else {
		user = context.get(EventContextKeys.PLAYER)
				.filter(ServerPlayer.class::isInstance)
				.map(ServerPlayer.class::cast)
				.map(ServerPlayer::user)
				.orElse(null);
//		}

		if (user == null) {
			if (event instanceof ExplosionEvent) {
				// Check igniter
				final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
				if (living instanceof User) {
					user = (User) living;
				}
			}
		}

		if(user == null)
		{
			if (sourceLocation == null)
				return;

			final Iterator<NotificationTicket> notificationTicketIterator = event.tickets().iterator();
			while(notificationTicketIterator.hasNext())
			{
				final NotificationTicket notificationTicket = notificationTicketIterator.next();
				final ServerLocation blockLocation = notificationTicket.target().location().get();
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

		User finalUser = user;
		final Iterator<NotificationTicket> notificationTicketIterator = event.tickets().iterator();
		while(notificationTicketIterator.hasNext())
		{
			final NotificationTicket notificationTicket = notificationTicketIterator.next();
			final ServerLocation blockLocation = notificationTicket.target().location().get();
			if(!super.getPlugin().getProtectionManager().canInteractWithBlock(blockLocation, finalUser, false).hasAccess())
			{
				notificationTicketIterator.remove();
			}
		}
	}
}
