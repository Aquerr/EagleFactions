package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionLeaveEventImpl extends FactionAbstractEvent implements FactionLeaveEvent
{
    FactionLeaveEventImpl(final Player player, final Faction faction, final Cause cause)
    {
        super(player, faction, cause);
    }
}
