package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionChestEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionChestEventImpl extends FactionAbstractEvent implements FactionChestEvent
{
    FactionChestEventImpl(final Player creator, final Faction faction, final Cause cause)
    {
        super(creator, faction, cause);
    }
}
