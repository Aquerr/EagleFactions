package io.github.aquerr.eaglefactions.util;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ServerUtils
{
    public static MinecraftServer getServer()
    {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
