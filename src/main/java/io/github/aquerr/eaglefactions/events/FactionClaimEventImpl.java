package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionClaimEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.world.World;
import org.spongepowered.math.vector.Vector3i;

public class FactionClaimEventImpl extends FactionUnclaimEventImpl implements FactionClaimEvent.Claim
{
    FactionClaimEventImpl(final Player creator, final Faction faction, final World world, final Vector3i chunkPosition, final Cause cause)
    {
        super(creator, faction, world, chunkPosition, cause);
    }

    @Override
    public boolean isClaimedByItems()
    {
        return EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig().shouldClaimByItems();
    }

    static class Pre extends FactionClaimEventImpl implements FactionClaimEvent.Claim.Pre
    {
        Pre(Player creator, Faction faction, World world, Vector3i chunkPosition, Cause cause)
        {
            super(creator, faction, world, chunkPosition, cause);
        }
    }

    static class Post extends FactionClaimEventImpl implements FactionClaimEvent.Claim.Post
    {
        Post(Player creator, Faction faction, World world, Vector3i chunkPosition, Cause cause)
        {
            super(creator, faction, world, chunkPosition, cause);
        }
    }
}
