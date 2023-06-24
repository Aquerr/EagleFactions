package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionCreateEvent;
import net.minecraft.server.level.ServerPlayer;

public class FactionCreateEventImpl extends FactionAbstractEvent implements FactionCreateEvent
{
    FactionCreateEventImpl(final ServerPlayer creator, final Faction faction)
    {
        super(creator, faction);
    }

    @Override
    public boolean isCreatedByItems()
    {
        //Factions cannot be created differently. All of them are created by items or none of them.
        return EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig().getFactionCreationByItems();
    }

    static class Pre extends FactionCreateEventImpl implements FactionCreateEvent.Pre
    {
        Pre(ServerPlayer creator, Faction faction)
        {
            super(creator, faction);
        }
    }

    static class Post extends FactionCreateEventImpl implements FactionCreateEvent.Post
    {
        Post(ServerPlayer creator, Faction faction)
        {
            super(creator, faction);
        }
    }
}
