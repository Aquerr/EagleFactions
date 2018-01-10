package io.github.aquerr.eaglefactions.entities;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;

public class FactionHome
{
    public Vector3i BlockPosition;
    public UUID WorldUUID;

    public FactionHome(UUID worldUUID, Vector3i blockPosition)
    {
        BlockPosition = blockPosition;
        WorldUUID = worldUUID;
    }
}
