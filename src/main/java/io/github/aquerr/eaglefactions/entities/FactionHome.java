package io.github.aquerr.eaglefactions.entities;

import com.flowpowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.UUID;

public class FactionHome
{
    private Vector3i BlockPosition;
    private UUID WorldUUID;

    public FactionHome(@Nullable UUID worldUUID, @Nullable Vector3i blockPosition)
    {
        this.BlockPosition = blockPosition;
        this.WorldUUID = worldUUID;
    }

    public FactionHome(String homeString)
    {
        String splitter = "\\|";
        String worldUUIDString = homeString.split(splitter)[0];
        String vectorsString = homeString.split(splitter)[1];

        String vectors[] = vectorsString.replace("(", "").replace(")", "").replace(" ", "").split(",");

        int x = Integer.valueOf(vectors[0]);
        int y = Integer.valueOf(vectors[1]);
        int z = Integer.valueOf(vectors[2]);

        Vector3i blockPosition = Vector3i.from(x, y, z);

        UUID worldUUID = UUID.fromString(worldUUIDString);
        this.BlockPosition = blockPosition;
        this.WorldUUID = worldUUID;

    }

    public UUID getWorldUUID()
    {
        return WorldUUID;
    }

    public Vector3i getBlockPosition()
    {
        return BlockPosition;
    }
}
