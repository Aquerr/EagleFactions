package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionJoinEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionJoinEventImpl extends FactionAbstractEvent implements FactionJoinEvent
{
    private final Cause cause;
    private final Player creator;
    private final Faction faction;

    FactionJoinEventImpl(final Player player, final Faction faction, final Cause cause)
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
