package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.FactionKickEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionKickEventImpl extends FactionAbstractEvent implements FactionKickEvent
{
    private final Cause cause;
    private final FactionPlayer kickedPlayer;
    private final Player creator;
    private final Faction faction;

    FactionKickEventImpl(final FactionPlayer kickedPlayer, final Player kickedBy, final Faction faction, final Cause cause)
    {
        super();
        this.kickedPlayer = kickedPlayer;
        this.creator = kickedBy;
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

    public FactionPlayer getKickedPlayer()
    {
        return this.kickedPlayer;
    }
}
