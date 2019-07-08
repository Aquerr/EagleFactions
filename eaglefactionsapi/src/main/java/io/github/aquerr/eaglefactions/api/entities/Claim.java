package io.github.aquerr.eaglefactions.api.entities;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;

public class Claim
{
    private final UUID worldUUID;
    private final Vector3i chunkPosition;

    public static Claim valueOf(String claimAsString)
    {
        String[] worldUUIDAndChunkPosition = claimAsString.split("\\|");
        UUID worldUUID = UUID.fromString(worldUUIDAndChunkPosition[0]);
        String vectors[] = worldUUIDAndChunkPosition[1].replace("(", "").replace(")", "").replace(" ", "").split(",");
        int x = Integer.valueOf(vectors[0]);
        int y = Integer.valueOf(vectors[1]);
        int z = Integer.valueOf(vectors[2]);
        Vector3i chunkPosition = Vector3i.from(x, y, z);
        return new Claim(worldUUID, chunkPosition);
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.worldUUID.hashCode();
        result = prime * result + this.chunkPosition.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Claim))
            return false;

        if (!((Claim) obj).worldUUID.equals(this.worldUUID))
            return false;

        if (!((Claim) obj).chunkPosition.equals(this.chunkPosition))
            return false;

        return true;
    }

    @Override
    public String toString()
    {
        return this.worldUUID.toString() + "|" + this.chunkPosition.toString();
    }
}
