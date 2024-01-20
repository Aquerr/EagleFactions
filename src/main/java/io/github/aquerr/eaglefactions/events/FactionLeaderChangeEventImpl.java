package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.FactionLeaderChangeEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;

public class FactionLeaderChangeEventImpl extends FactionAbstractEvent implements FactionLeaderChangeEvent
{
    private final FactionPlayer newLeader;

    FactionLeaderChangeEventImpl(Player creator, FactionPlayer newLeader, Faction faction, Cause cause)
    {
        super(creator, faction, cause);
        this.newLeader = newLeader;
    }

    @Override
    public @Nullable FactionPlayer getNewLeader()
    {
        return newLeader;
    }

    static class Pre extends FactionLeaderChangeEventImpl implements FactionLeaderChangeEvent.Pre
    {
        Pre(Player creator, final FactionPlayer newLeader, Faction faction, Cause cause)
        {
            super(creator, newLeader, faction, cause);
        }
    }

    static class Post extends FactionLeaderChangeEventImpl implements FactionLeaderChangeEvent.Post
    {
        Post(Player creator, FactionPlayer newLeader, Faction faction, Cause cause)
        {
            super(creator, newLeader, faction, cause);
        }
    }
}
