package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

abstract class FactionAbstractEvent extends AbstractEvent implements FactionEvent
{
    private final Faction faction;
    private final Player creator;
    private final Cause cause;

    private boolean isCancelled = false;

    protected FactionAbstractEvent(final Player creator, final Faction faction, final Cause cause)
    {
        this.creator = creator;
        this.faction = faction;
        this.cause = cause;
    }

    @Override
    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public Player getCreator()
    {
        return this.creator;
    }

    @Override
    public Cause getCause()
    {
        return this.cause;
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
}
