package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionLeaveEventImpl extends FactionAbstractEvent implements FactionLeaveEvent
{
    private final Cause cause;
    private final Player creator;
    private final Faction faction;

    FactionLeaveEventImpl(final Player player, final Faction faction, final Cause cause)
    {
        super();
        this.creator = player;
        this.faction = faction;
        this.cause = cause;
    }

    @Override
    public Cause getCause()
    {
        return this.cause;
    }

    @Override
    public Player getCreator()
    {
        return this.creator;
    }

    public Faction getFaction()
    {
        return this.faction;
    }
}
