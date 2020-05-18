package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionDisbandEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionAreaEnterEventImp extends FactionAbstractEvent implements FactionDisbandEvent
{
    private final Cause cause;
    private final Player player;
    private final Faction faction;

    public FactionAreaEnterEventImp(Player player, Faction playerFaction, Cause cause)
    {
        this.player = player;
        this.faction = playerFaction;
        this.cause = cause;
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
