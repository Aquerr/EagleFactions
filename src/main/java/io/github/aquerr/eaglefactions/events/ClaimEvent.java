package io.github.aquerr.eaglefactions.events;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.World;

public class ClaimEvent extends AbstractEvent
{
    private final Cause _cause;
    private final Player _creator;
    private final Faction _faction;
    private final World _world;
    private final Vector3i _chunkPosition;

    public ClaimEvent(Player creator, Faction faction, World world, Vector3i chunkPosition, Cause cause)
    {
        this._creator = creator;
        this._faction = faction;
        this._cause = cause;
        this._world = world;
        this._chunkPosition = chunkPosition;
    }

    @Override
    public Cause getCause()
    {
        return null;
    }

    public Faction getFaction()
    {
        return _faction;
    }

    public Player getCreator()
    {
        return _creator;
    }

    public World getWorld()
    {
        return _world;
    }

    public Vector3i getChunkPosition()
    {
        return _chunkPosition;
    }
}
