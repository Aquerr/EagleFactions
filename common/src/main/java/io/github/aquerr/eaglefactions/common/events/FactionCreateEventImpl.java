package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionCreateEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionCreateEventImpl extends FactionAbstractEvent implements FactionCreateEvent
{
    private final Cause cause;
    private final Player creator;
    private final Faction faction;

    FactionCreateEventImpl(final Player creator, final Faction faction, final Cause cause)
    {
        super();
        this.creator = creator;
        this.faction = faction;
        this.cause = cause;
    }

    @Override
    public Cause getCause()
    {
        return this.cause;
    }

    public Faction getFaction()
    {
        return this.faction;
    }

    public Player getCreator()
    {
        return this.creator;
    }

    /**
     * Not yet implemented...
     */
    //TODO: To implement...
    @Override
    public boolean isCreatedByItems()
    {
        return false;
    }
}
