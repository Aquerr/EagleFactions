package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionAreaEnterEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class FactionAreaEnterEventImpl extends AbstractEvent implements FactionAreaEnterEvent
{
	private final MoveEntityEvent moveEntityEvent;

	private final Cause cause;
	private final Player creator;
	private final Optional<Faction> enteredFaction;
	private final Optional<Faction> leftFaction;

	private boolean isCancelled = false;

	public FactionAreaEnterEventImpl(final MoveEntityEvent moveEntityEvent, final Player creator, final Optional<Faction> enteredFaction, final Optional<Faction> leftFaction, final Cause cause)
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
	public Cause getCause()
	{
		return this.cause;
	}

	@Override
	public Transform<World> getFromTransform()
	{
		return this.moveEntityEvent.getFromTransform();
	}

	@Override
	public Transform<World> getToTransform()
	{
		return this.moveEntityEvent.getToTransform();
	}

	@Override
	public void setToTransform(Transform<World> transform)
	{
		this.moveEntityEvent.setToTransform(transform);
	}

	@Override
	public Entity getTargetEntity()
	{
		return this.creator;
	}
}
