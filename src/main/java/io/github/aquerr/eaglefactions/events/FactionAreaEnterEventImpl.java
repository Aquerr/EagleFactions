package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionAreaEnterEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class FactionAreaEnterEventImpl extends AbstractEvent implements FactionAreaEnterEvent
{
	private final MoveEntityEvent moveEntityEvent;

	private final Cause cause;
	private final Player creator;
	private final Optional<Faction> enteredFaction;
	private final Optional<Faction> leftFaction;

	private boolean isCancelled = false;

	FactionAreaEnterEventImpl(final MoveEntityEvent moveEntityEvent, final Player creator, final Optional<Faction> enteredFaction, final Optional<Faction> leftFaction, final Cause cause)
	{
		this.moveEntityEvent = moveEntityEvent;
		this.creator = creator;
		this.cause = cause;
		this.enteredFaction = enteredFaction;
		this.leftFaction = leftFaction;
	}

	@Override
	public Player getCreator()
	{
		return this.creator;
	}

	@Override
	public Optional<Faction> getEnteredFaction()
	{
		return this.enteredFaction;
	}

	@Override
	public Optional<Faction> getLeftFaction()
	{
		return this.leftFaction;
	}

	@Override
	public boolean isCancelled()
	{
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		this.isCancelled = cancelled;
	}

	@Override
	public Cause cause()
	{
		return this.cause;
	}

	@Override
	public Vector3d getOriginalPosition()
	{
		return this.moveEntityEvent.originalPosition();
	}

	@Override
	public Vector3d getDestinationPosition()
	{
		return this.moveEntityEvent.destinationPosition();
	}

	@Override
	public void setDestinationPosition(Vector3d position)
	{
		this.moveEntityEvent.setDestinationPosition(position);
	}

	static class Pre extends FactionAreaEnterEventImpl implements FactionAreaEnterEvent.Pre
	{
		Pre(MoveEntityEvent moveEntityEvent, Player creator, Optional<Faction> enteredFaction, Optional<Faction> leftFaction, Cause cause)
		{
			super(moveEntityEvent, creator, enteredFaction, leftFaction, cause);
		}
	}

	static class Post extends FactionAreaEnterEventImpl implements FactionAreaEnterEvent.Post
	{
		Post(MoveEntityEvent moveEntityEvent, Player creator, Optional<Faction> enteredFaction, Optional<Faction> leftFaction, Cause cause)
		{
			super(moveEntityEvent, creator, enteredFaction, leftFaction, cause);
		}
	}
}
