package io.github.aquerr.eaglefactions.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.UUID;

public final class WorldUtil
{
    private WorldUtil()
    {
        throw new UnsupportedOperationException();
    }

    public static Optional<ServerWorld> getWorldByUUID(final UUID uuid)
    {
        return Sponge.server().worldManager().worlds().stream()
                .filter(serverWorld -> serverWorld.uniqueId().equals(uuid))
                .findFirst();
    }

    public static String getPlainWorldName(final ServerWorld serverWorld)
    {
        return serverWorld.properties().name();
    }

    public static ServerLocation getBlockTopCenter(ServerLocation blockLocation)
    {
        double centerX = blockLocation.blockX() > 0 ? blockLocation.blockX() + 0.5 : blockLocation.blockX() - 0.5;
        double centerY = blockLocation.blockY();
        double centerZ = blockLocation.blockZ() > 0 ? blockLocation.blockZ() + 0.5 : blockLocation.blockZ() - 0.5;
        return ServerLocation.of(blockLocation.world(), Vector3d.from(centerX, centerY, centerZ));
    }

    public static Vector3d getChunkTopCenter(final ServerWorld world, final Vector3i chunkPosition)
    {
        final double x = (chunkPosition.x() << 4) + 8;
        final double z = (chunkPosition.z() << 4) + 8;
        final double y = world.highestYAt((int)x, (int)z);
        return new Vector3d(x, y, z);
    }
}
