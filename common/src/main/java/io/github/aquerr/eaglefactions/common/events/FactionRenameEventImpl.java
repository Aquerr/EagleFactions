package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionRenameEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionRenameEventImpl extends FactionAbstractEvent implements FactionRenameEvent
{
    private final String newName;

    FactionRenameEventImpl(final Player player, final Faction faction, final String newName, final Cause cause)
    {
        super(player, faction, cause);
        this.newName = newName;
    }

    @Override
    public String getNewFactionName()
    {
        return this.newName;
    }
}
