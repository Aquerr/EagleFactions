package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionTagUpdateEvent;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;

public class FactionTagUpdateEventImpl extends FactionAbstractEvent implements FactionTagUpdateEvent
{
    private final TextComponent oldTag;
    private final String newTag;

    FactionTagUpdateEventImpl(final Player player, final Faction faction, final Cause cause, final TextComponent oldTag, final String newTag)
    {
        super(player, faction, cause);
        this.oldTag = oldTag;
        this.newTag = newTag;
    }

    @Override
    public TextComponent getOldTag()
    {
        return oldTag;
    }

    @Override
    public String getNewTag()
    {
        return newTag;
    }

    static class Pre extends FactionTagUpdateEventImpl implements FactionTagUpdateEvent.Pre
    {
        Pre(Player player, Faction faction, Cause cause, TextComponent oldTag, String newTag)
        {
            super(player, faction, cause, oldTag, newTag);
        }
    }

    static class Post extends FactionTagUpdateEventImpl implements FactionTagUpdateEvent.Post
    {
        Post(Player player, Faction faction, Cause cause, TextComponent oldTag, String newTag)
        {
            super(player, faction, cause, oldTag, newTag);
        }
    }
}
