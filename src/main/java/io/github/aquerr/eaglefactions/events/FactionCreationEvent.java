package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class FactionCreationEvent extends AbstractEvent
{
    private final Cause _cause;
    private final Player _creator;
    private final Faction _faction;

    public FactionCreationEvent(Player creator, Faction faction, Cause cause)
    {
        this._creator = creator;
        this._faction = faction;
        this._cause = cause;
    }

    @Override
    public Cause getCause()
    {
        return null;
    }

    public Faction getFaction()
    {
        return _faction;
    }

    public Player getCreator()
    {
        return _creator;
    }
}
