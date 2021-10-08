package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionJoinEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionJoinEventImpl extends FactionAbstractEvent implements FactionJoinEvent
{
    FactionJoinEventImpl(final Player creator, final Faction faction, final Cause cause)
    {
        super(creator, faction, cause);
    }

    static class Pre extends FactionJoinEventImpl implements FactionJoinEvent.Pre
    {
        Pre(Player creator, Faction faction, Cause cause)
        {
            super(creator, faction, cause);
        }
    }

    static class Post extends FactionJoinEventImpl implements FactionJoinEvent.Post
    {
        Post(Player creator, Faction faction, Cause cause)
        {
            super(creator, faction, cause);
        }
    }
}
