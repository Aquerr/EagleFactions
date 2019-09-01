package io.github.aquerr.eaglefactions.api.events;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.World;

public interface FactionClaimEvent extends FactionEvent
{
    /**
     * Gets the world in which the claim event was triggered in.
     * @return {@link World} object.
     */
    World getWorld();

    /**
     * Gets the chunk position of the claim where the claim event was triggered in.
     * @return chunk position as {@link Vector3i}
     */
    Vector3i getChunkPosition();

    interface Claim
    {
        /**
         * <strong>Currently unimplemented</strong>
         *
         * @return <tt>true</tt> if territory is being claimed by items or <tt>false</tt> if it is not
         */
        boolean isClaimedByItems();
    }

    interface Unclaim
    {

    }
}
