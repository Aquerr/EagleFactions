package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.math.Vector3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * A util class used for running Eagle Factions events.
 */
public final class EventRunner
{
    private EventRunner()
    {

    }


    public static boolean runFactionCreateEventPre(ServerPlayer player, Faction faction)
    {
        return postEvent(new FactionCreateEventImpl.Pre(player, faction));
    }

    public static void runFactionCreateEventPost(ServerPlayer player, Faction faction)
    {
        postEvent(new FactionCreateEventImpl.Post(player, faction));
    }

    public static void runFactionClaimEventPost(ServerPlayer player, Faction faction, ServerLevel level, Vector3i chunkPosition)
    {
        postEvent(new FactionClaimEventImpl.Post(player, faction, level, chunkPosition));
    }

    private static boolean postEvent(Event event)
    {
        return MinecraftForge.EVENT_BUS.post(event);
    }
}
