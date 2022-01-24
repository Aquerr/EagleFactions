package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;

public class FactionLeaveEventImpl extends FactionAbstractEvent implements FactionLeaveEvent
{
    FactionLeaveEventImpl(final Player player, final Faction faction, final Cause cause)
    {
        super(player, faction, cause);
    }

    static class Pre extends FactionLeaveEventImpl implements FactionLeaveEvent.Pre
    {
        Pre(Player player, Faction faction, Cause cause)
        {
            super(player, faction, cause);
        }
    }

    static class Post extends FactionLeaveEventImpl implements FactionLeaveEvent.Post
    {
        Post(Player player, Faction faction, Cause cause)
        {
            super(player, faction, cause);
        }
    }
}
