package io.github.aquerr.eaglefactions.common.events;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionClaimEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;

public class FactionClaimEventImpl extends FactionUnclaimEventImpl implements FactionClaimEvent.Claim
{
    FactionClaimEventImpl(final Player creator, final Faction faction, final World world, final Vector3i chunkPosition, final Cause cause)
    {
        super(creator, faction, world, chunkPosition, cause);
    }

    //TODO: Implement this method...
    @Override
    public boolean isClaimedByItems()
    {
        return false;
    }
}
