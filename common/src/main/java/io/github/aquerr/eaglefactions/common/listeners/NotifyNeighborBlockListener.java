package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
		final Cause cause = event.getCause();
		final EventContext context = event.getContext();
		final TileEntity tileEntity = event.getCause().first(TileEntity.class).orElse(null);
		final LocatableBlock locatableBlock = cause.first(LocatableBlock.class).orElse(null);
		Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;

		User user;
		if (cause.root() instanceof TileEntity) {
			user = context.get(EventContextKeys.OWNER)
					.orElse(context.get(EventContextKeys.NOTIFIER)
							.orElse(context.get(EventContextKeys.CREATOR)
									.orElse(null)));
		} else {
			user = context.get(EventContextKeys.NOTIFIER)
					.orElse(context.get(EventContextKeys.OWNER)
							.orElse(context.get(EventContextKeys.CREATOR)
									.orElse(null)));
		}

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

			final Iterator<Direction> directionIterator = event.getNeighbors().keySet().iterator();
			while(directionIterator.hasNext())
			{
				final Direction direction = directionIterator.next();
				final Location<World> blockLocation = sourceLocation.getBlockRelative(direction);
				if(!super.getPlugin().getProtectionManager().canNotifyBlock(sourceLocation, blockLocation).hasAccess())
				{
					directionIterator.remove();
				}
			}
			return;
		}

		if(sourceLocation == null)
		{
			final Player player = event.getCause().first(Player.class).orElse(null);
			if(player == null)
				return;

			sourceLocation = player.getLocation();
		}

		if(!super.getPlugin().getProtectionManager().canInteractWithBlock(sourceLocation, user, false).hasAccess())
		{
			event.setCancelled(true);
			return;
		}

		Location<World> finalSourceLocation = sourceLocation;
		User finalUser = user;
		final Iterator<Direction> directionIterator = event.getNeighbors().keySet().iterator();
		while(directionIterator.hasNext())
		{
			final Direction direction = directionIterator.next();
			final Location<World> blockLocation = finalSourceLocation.getBlockRelative(direction);
			if(!super.getPlugin().getProtectionManager().canInteractWithBlock(blockLocation, finalUser, false).hasAccess())
			{
				directionIterator.remove();
			}
		}
	}
}
