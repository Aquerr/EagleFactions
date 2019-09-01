package io.github.aquerr.eaglefactions.common.events;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.World;

/**
 * An util class used for running Eagle Factions events.
 */
public final class EventRunner
{
    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionLeaveEvent(final Player player, final Faction faction)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, player)
                .add(EventContextKeys.PLAYER, player)
                .add(EventContextKeys.CREATOR, player)
                .build();

        final Cause creationEventCause = Cause.of(eventContext, player);
        final FactionLeaveEvent event = new FactionLeaveEventImpl(player, faction, creationEventCause);
        return Sponge.getEventManager().post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionChestEvent(final Player player, final Faction faction)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, player)
                .add(EventContextKeys.PLAYER, player)
                .add(EventContextKeys.CREATOR, player)
                .build();

        final Cause eventCause = Cause.of(eventContext, player);
        final FactionChestEventImpl event = new FactionChestEventImpl(player, faction, eventCause);
        return Sponge.getEventManager().post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionClaimEvent(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, player)
                .add(EventContextKeys.PLAYER, player)
                .add(EventContextKeys.CREATOR, player)
                .build();

        final Cause creationEventCause = Cause.of(eventContext, player);
        final FactionClaimEventImpl event = new FactionClaimEventImpl(player, faction, world, chunkPosition, creationEventCause);
        return Sponge.getEventManager().post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionCreateEvent(final Player player, final Faction faction)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, player)
                .add(EventContextKeys.PLAYER, player)
                .add(EventContextKeys.CREATOR, player)
                .build();

        final Cause creationEventCause = Cause.of(eventContext, player);
        final FactionCreateEventImpl event = new FactionCreateEventImpl(player, faction, creationEventCause);
        return Sponge.getEventManager().post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionKickEvent(final IFactionPlayer kickedPlayer, final Player kickedBy, final Faction faction)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, kickedBy)
                .add(EventContextKeys.PLAYER, kickedBy)
                .add(EventContextKeys.CREATOR, kickedBy)
                .build();

        final Cause creationEventCause = Cause.of(eventContext, kickedBy);
        final FactionKickEventImpl event = new FactionKickEventImpl(kickedPlayer, kickedBy, faction, creationEventCause);
        return Sponge.getEventManager().post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionUnclaimEvent(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, player)
                .add(EventContextKeys.PLAYER, player)
                .add(EventContextKeys.CREATOR, player)
                .build();

        final Cause creationEventCause = Cause.of(eventContext, player);
        final FactionUnclaimEventImpl event = new FactionUnclaimEventImpl(player, faction, world, chunkPosition, creationEventCause);
        return Sponge.getEventManager().post(event);
    }
}
