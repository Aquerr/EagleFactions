package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionDisbandEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionDisbandEventImpl extends FactionAbstractEvent implements FactionDisbandEvent
{
    private final boolean forceRemovedByAdmin;
    private final boolean removedByFactionsRemover;

    FactionDisbandEventImpl(final Player creator, final Faction faction, final boolean forceRemovedByAdmin, final boolean removedByFactionsRemover, final Cause cause)
    {
        super(creator, faction, cause);
        this.forceRemovedByAdmin = forceRemovedByAdmin;
        this.removedByFactionsRemover = removedByFactionsRemover;
    }

    @Override
    public boolean removedDueToInactivity()
    {
        return this.removedByFactionsRemover;
    }

    @Override
    public boolean forceRemovedByAdmin()
    {
        return this.forceRemovedByAdmin;
    }
}
