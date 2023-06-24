package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public abstract class FactionAbstractEvent extends Event implements FactionEvent
{
    private final Faction faction;
    private final ServerPlayer creator;

    private boolean isCancelled = false;

    protected FactionAbstractEvent(final ServerPlayer creator, final Faction faction)
    {
        this.creator = creator;
        this.faction = faction;
    }

    @Override
    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public ServerPlayer getCreator()
    {
        return this.creator;
    }

    @Override
    public boolean isCancelled()
    {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.isCancelled = cancelled;
    }
}
