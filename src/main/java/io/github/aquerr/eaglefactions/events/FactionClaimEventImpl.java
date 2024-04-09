package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionClaimEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

public class FactionClaimEventImpl extends FactionAbstractEvent implements FactionClaimEvent.Claim
{
    private final ServerWorld world;
    private final Vector3i chunkPosition;

    FactionClaimEventImpl(final Player creator, final Faction faction, final ServerWorld world, final Vector3i chunkPosition, final Cause cause)
    {
        super(creator, faction, cause);
        this.world = world;
        this.chunkPosition = chunkPosition;
    }

    @Override
    public ServerWorld getWorld()
    {
        return world;
    }

    @Override
    public Vector3i getChunkPosition()
    {
        return chunkPosition;
    }

    static class Pre extends FactionClaimEventImpl implements FactionClaimEvent.Claim.Pre
    {
        Pre(Player creator, Faction faction, ServerWorld world, Vector3i chunkPosition, Cause cause)
        {
            super(creator, faction, world, chunkPosition, cause);
        }
    }

    static class Post extends FactionClaimEventImpl implements FactionClaimEvent.Claim.Post
    {
        Post(Player creator, Faction faction, ServerWorld world, Vector3i chunkPosition, Cause cause)
        {
            super(creator, faction, world, chunkPosition, cause);
        }
    }
}
