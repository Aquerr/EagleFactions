package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionTagColorUpdateEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;

public class FactionTagColorUpdateEventImpl extends FactionAbstractEvent implements FactionTagColorUpdateEvent
{
    private final NamedTextColor oldColor;
    private final NamedTextColor newColor;

    FactionTagColorUpdateEventImpl(final Player player, final Faction faction, final Cause cause, final NamedTextColor oldColor, final NamedTextColor newColor)
    {
        super(player, faction, cause);
        this.oldColor = oldColor;
        this.newColor = newColor;
    }

    @Override
    public NamedTextColor getOldColor()
    {
        return this.oldColor;
    }

    @Override
    public NamedTextColor getNewColor()
    {
        return this.newColor;
    }

    static class Pre extends FactionTagColorUpdateEventImpl implements FactionTagColorUpdateEvent.Pre
    {
        Pre(Player player, Faction faction, Cause cause, NamedTextColor oldColor, NamedTextColor newColor)
        {
            super(player, faction, cause, oldColor, newColor);
        }
    }

    static class Post extends FactionTagColorUpdateEventImpl implements FactionTagColorUpdateEvent.Post
    {
        Post(Player player, Faction faction, Cause cause, NamedTextColor oldColor, NamedTextColor newColor)
        {
            super(player, faction, cause, oldColor, newColor);
        }
    }
}
