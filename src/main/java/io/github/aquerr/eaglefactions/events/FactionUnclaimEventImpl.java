package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionClaimEvent;
import io.github.aquerr.eaglefactions.api.math.Vector3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FactionUnclaimEventImpl extends FactionAbstractEvent implements FactionClaimEvent.Unclaim
{
    private final Level world;
    private final Vector3i chunkPosition;

    FactionUnclaimEventImpl(final ServerPlayer creator, final Faction faction, final Level world, final Vector3i chunkPosition)
    {
        super(creator, faction);
        this.world = world;
        this.chunkPosition = chunkPosition;
    }

    public Level getWorld()
    {
        return this.world;
    }

    public Vector3i getChunkPosition()
    {
        return this.chunkPosition;
    }

    static class Pre extends FactionUnclaimEventImpl implements FactionClaimEvent.Unclaim.Pre
    {
        Pre(ServerPlayer creator, Faction faction, Level world, Vector3i chunkPosition)
        {
            super(creator, faction, world, chunkPosition);
        }
    }

    static class Post extends FactionUnclaimEventImpl implements FactionClaimEvent.Unclaim.Post
    {
        Post(ServerPlayer creator, Faction faction, Level world, Vector3i chunkPosition)
        {
            super(creator, faction, world, chunkPosition);
        }
    }
}