package io.github.aquerr.eaglefactions.entities;

import com.flowpowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.UUID;

public class FactionHome
{
    public Vector3i BlockPosition;
    public UUID WorldUUID;

    public FactionHome(@Nullable UUID worldUUID, @Nullable Vector3i blockPosition)
    {
        BlockPosition = blockPosition;
        WorldUUID = worldUUID;
    }
}
