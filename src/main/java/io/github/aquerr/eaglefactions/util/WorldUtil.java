package io.github.aquerr.eaglefactions.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.util.Optional;
import java.util.UUID;

public final class WorldUtil
{
    private WorldUtil()
    {

    }

    public static Optional<Level> getWorldByUUID(final UUID uuid)
    {
        return Optional.empty();
//        for (Level level : ServerUtils.getServer().getAllLevels())
//        {
//            if (level)
//        }
//
//        ServerUtils.getServer().getAllLevels();
//
//        return ServerUtils.getServer().getAllLevels()
//                .filter(serverWorld -> serverWorld.uniqueId().equals(uuid))
//                .findFirst();
    }

    public static String getPlainWorldName(final Level level)
    {
        return level.getServer().getWorldPath(LevelResource.ROOT).getFileName().toString();
    }
}
