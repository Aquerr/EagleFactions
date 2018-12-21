package io.github.aquerr.eaglefactions.entities;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;


//TODO: Try using this new class.
public class Claim
{
    private final UUID worldUUID;
    private final Vector3i chunkPosition;

    public Claim(UUID worldUUID, Vector3i chunkPosition)
    {
        this.worldUUID = worldUUID;
        this.chunkPosition = chunkPosition;
    }

    public UUID getWorldUUID()
    {
        return this.worldUUID;
    }

    public Vector3i getChunkPosition()
    {
        return this.chunkPosition;
    }
}
