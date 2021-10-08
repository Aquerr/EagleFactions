package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.FactionDemoteEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionDemoteEventImpl extends FactionAbstractEvent implements FactionDemoteEvent
{
    private final FactionPlayer demotedPlayer;

    FactionDemoteEventImpl(final Faction faction, final Player demotedBy, final FactionPlayer demotedPlayer, final Cause cause)
    {
        super(demotedBy, faction, cause);
        this.demotedPlayer = demotedPlayer;
    }

    @Override
    public FactionPlayer getDemotedPlayer()
    {
        return this.demotedPlayer;
    }

    static class Pre extends FactionDemoteEventImpl implements FactionDemoteEvent.Pre
    {
        Pre(Faction faction, Player demotedBy, FactionPlayer demotedPlayer, Cause cause)
        {
            super(faction, demotedBy, demotedPlayer, cause);
        }
    }

    static class Post extends FactionDemoteEventImpl implements FactionDemoteEvent.Post
    {
        private final FactionMemberType demotedToRank;

        Post(Faction faction, Player demotedBy, FactionPlayer demotedPlayer, FactionMemberType demotedToRank, Cause cause)
        {
            super(faction, demotedBy, demotedPlayer, cause);
            this.demotedToRank = demotedToRank;
        }

        @Override
        public FactionMemberType getDemotedToRank()
        {
            return this.demotedToRank;
        }
    }
}
