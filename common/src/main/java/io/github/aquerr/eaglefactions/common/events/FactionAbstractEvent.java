package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.events.FactionEvent;
import org.spongepowered.api.event.impl.AbstractEvent;

abstract class FactionAbstractEvent extends AbstractEvent implements FactionEvent
{
    private boolean isCancelled = false;

    FactionAbstractEvent()
    {
        super();
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
