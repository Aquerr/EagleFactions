package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class FactionKickEvent extends AbstractEvent
{
    private final Cause _cause;
    private final IFactionPlayer _kickedPlayer;
    private final Faction _faction;

    public FactionKickEvent(IFactionPlayer kickedPlayer, Faction faction, Cause cause)
    {
        this._kickedPlayer = kickedPlayer;
        this._faction = faction;
        this._cause = cause;
    }

    @Override
    public Cause getCause()
    {
        return this._cause;
    }

    public Faction getFaction()
    {
        return this._faction;
    }

    public IFactionPlayer getKickedPlayer()
    {
        return this._kickedPlayer;
    }
}
