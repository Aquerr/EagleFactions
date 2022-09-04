package io.github.aquerr.eaglefactions.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;
import java.util.UUID;

public class WorldUtil
{
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
}