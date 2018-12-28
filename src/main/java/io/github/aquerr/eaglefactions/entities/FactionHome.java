package io.github.aquerr.eaglefactions.entities;

import com.flowpowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.UUID;

public class FactionHome
{
    private final Vector3i BlockPosition;
    private final UUID WorldUUID;

    public FactionHome(@Nullable UUID worldUUID, @Nullable Vector3i blockPosition)
    {
        this.BlockPosition = blockPosition;
        this.WorldUUID = worldUUID;
    }

    public static FactionHome from(String worldUUIDAndBlockPositionString)
    {
        try
        {
            String splitter = "\\|";
            String worldUUIDString = worldUUIDAndBlockPositionString.split(splitter)[0];
            String vectorsString = worldUUIDAndBlockPositionString.split(splitter)[1];

            String[] vectors = vectorsString.replace("(", "").replace(")", "").replace(" ", "").split(",");

            int x = Integer.valueOf(vectors[0]);
            int y = Integer.valueOf(vectors[1]);
            int z = Integer.valueOf(vectors[2]);

            Vector3i blockPosition = Vector3i.from(x, y, z);

            UUID worldUUID = UUID.fromString(worldUUIDString);
            return new FactionHome(worldUUID, blockPosition);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return null;
        }
    }

    public UUID getWorldUUID()
    {
        return WorldUUID;
    }

    public Vector3i getBlockPosition()
    {
        return BlockPosition;
    }

    @Override
    public String toString()
    {
        return this.WorldUUID.toString() + "|" + this.BlockPosition.toString();
    }
}
