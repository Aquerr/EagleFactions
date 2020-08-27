package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionRenameEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class FactionRenameEventImpl extends FactionAbstractEvent implements FactionRenameEvent
{
    private final String newName;

    FactionRenameEventImpl(final Player player, final Faction faction, final String newName, final Cause cause)
    {
        super(player, faction, cause);
        this.newName = newName;
    }

    @Override
    public String getNewFactionName()
    {
        return this.newName;
    }

    static class Pre extends FactionRenameEventImpl implements FactionRenameEvent.Pre
    {
        Pre(Player player, Faction faction, String newName, Cause cause)
        {
            super(player, faction, newName, cause);
        }
    }

    static class Post extends FactionRenameEventImpl implements FactionRenameEvent.Post
    {
        Post(Player player, Faction faction, String newName, Cause cause)
        {
            super(player, faction, newName, cause);
        }
    }
}
