package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionInviteEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionInviteEventImpl extends FactionAbstractEvent implements FactionInviteEvent
{
    private final Player invitedPlayer;

    FactionInviteEventImpl(final Player creator, final Player invitedPlayer, final Faction faction, final Cause cause)
    {
        super(creator, faction, cause);
        this.invitedPlayer = invitedPlayer;
    }

    @Override
    public Player getInvitedPlayer()
    {
        return this.invitedPlayer;
    }

    static class Pre extends FactionInviteEventImpl implements FactionInviteEvent.Pre
    {
        Pre(Player creator, Player invitedPlayer, Faction faction, Cause cause)
        {
            super(creator, invitedPlayer, faction, cause);
        }
    }

    static class Post extends FactionInviteEventImpl implements FactionInviteEvent.Post
    {
        Post(Player creator, Player invitedPlayer, Faction faction, Cause cause)
        {
            super(creator, invitedPlayer, faction, cause);
        }
    }
}
