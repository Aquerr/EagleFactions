package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionCreateEvent;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionCreateEventImpl extends FactionAbstractEvent implements FactionCreateEvent
{
    private final Cause cause;
    private final Player creator;
    private final Faction faction;

    FactionCreateEventImpl(final Player creator, final Faction faction, final Cause cause)
    {
        super();
        this.creator = creator;
        this.faction = faction;
        this.cause = cause;
    }

    @Override
    public Cause getCause()
    {
        return this.cause;
    }

    @Override
    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public Player getCreator()
    {
        return this.creator;
    }

    @Override
    public boolean isCreatedByItems()
    {
        //Factions cannot be created differently. All of them are created by items or none of them.
        return EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig().getFactionCreationByItems();
    }
}
