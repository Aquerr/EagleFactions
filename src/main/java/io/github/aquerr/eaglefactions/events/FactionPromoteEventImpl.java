package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.FactionPromoteEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;

public class FactionPromoteEventImpl extends FactionAbstractEvent implements FactionPromoteEvent
{
    private final FactionPlayer promotedPlayer;

    FactionPromoteEventImpl(Player creator, FactionPlayer promotedPlayer, Faction faction, Cause cause)
    {
        super(creator, faction, cause);
        this.promotedPlayer = promotedPlayer;
    }

    @Override
    public FactionPlayer getPromotedPlayer()
    {
        return this.promotedPlayer;
    }

    static class Pre extends FactionPromoteEventImpl implements FactionPromoteEvent.Pre
    {
        Pre(Player creator, final FactionPlayer promotedPlayer, Faction faction, Cause cause)
        {
            super(creator, promotedPlayer, faction, cause);
        }
    }

    static class Post extends FactionPromoteEventImpl implements FactionPromoteEvent.Post
    {
        private final FactionMemberType promotedToRank;

        Post(Player creator, FactionPlayer promotedPlayer, Faction faction, final FactionMemberType promotedToRank, Cause cause)
        {
            super(creator, promotedPlayer, faction, cause);
            this.promotedToRank = promotedToRank;
        }

        @Override
        public FactionMemberType getPromotedToRank()
        {
            return this.promotedToRank;
        }
    }
}
