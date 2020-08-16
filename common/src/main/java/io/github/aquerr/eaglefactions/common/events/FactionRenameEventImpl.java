package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionRenameEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionRenameEventImpl extends FactionAbstractEvent implements FactionRenameEvent
{
    private final Cause cause;
    private final Player player;
    private final Faction faction;
    private final String newName;

    public FactionRenameEventImpl(final Player player, final Faction faction, final String newName, final Cause cause)
    {
        this.player = player;
        this.faction = faction;
        this.newName = newName;
        this.cause = cause;
    }

    @Override
    public String getNewFactionName()
    {
        return this.newName;
    }

    @Override
    public Player getCreator()
    {
        return this.player;
    }

    @Override
    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public Cause getCause()
    {
        return this.cause;
    }
}
