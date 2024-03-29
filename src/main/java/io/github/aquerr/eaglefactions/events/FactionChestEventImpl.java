package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionChestEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;

public class FactionChestEventImpl extends FactionAbstractEvent implements FactionChestEvent
{
    FactionChestEventImpl(final Player creator, final Faction faction, final Cause cause)
    {
        super(creator, faction, cause);
    }

    static class Pre extends FactionChestEventImpl implements FactionChestEvent.Pre
    {
        Pre(Player creator, Faction faction, Cause cause)
        {
            super(creator, faction, cause);
        }
    }

    static class Post extends FactionChestEventImpl implements FactionChestEvent.Post
    {
        Post(Player creator, Faction faction, Cause cause)
        {
            super(creator, faction, cause);
        }
    }
}
