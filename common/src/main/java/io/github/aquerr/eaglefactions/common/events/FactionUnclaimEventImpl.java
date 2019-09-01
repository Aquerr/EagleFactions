package io.github.aquerr.eaglefactions.common.events;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionClaimEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;

public class FactionUnclaimEventImpl extends FactionAbstractEvent implements FactionClaimEvent.Unclaim
{
    private final Cause cause;
    private final Player creator;
    private final Faction faction;
    private final World world;
    private final Vector3i chunkPosition;

    FactionUnclaimEventImpl(final Player creator, final Faction faction, final World world, final Vector3i chunkPosition, final Cause cause)
    {
        super();
        this.creator = creator;
        this.faction = faction;
        this.cause = cause;
        this.world = world;
        this.chunkPosition = chunkPosition;
    }

    @Override
    public Cause getCause()
    {
        return this.cause;
    }

    public Faction getFaction()
    {
        return this.faction;
    }

    public Player getCreator()
    {
        return this.creator;
    }

    public World getWorld()
    {
        return this.world;
    }

    public Vector3i getChunkPosition()
    {
        return this.chunkPosition;
    }
}
