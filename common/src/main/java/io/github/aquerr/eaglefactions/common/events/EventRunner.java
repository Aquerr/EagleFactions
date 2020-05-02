package io.github.aquerr.eaglefactions.common.events;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.World;

import java.util.Optional;

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

        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionLeaveEvent event = new FactionLeaveEventImpl(player, faction, eventCause);
        return Sponge.getEventManager().post(event);
    }

    public static boolean runFactionJoinEvent(final Player player, final Faction faction)
    {
        final EventContext eventContext = EventContext.builder()
            .add(EventContextKeys.OWNER, player)
            .add(EventContextKeys.PLAYER, player)
            .add(EventContextKeys.CREATOR, player)
            .build();

        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionJoinEvent event = new FactionJoinEventImpl(player, faction, eventCause);
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

        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionChestEvent event = new FactionChestEventImpl(player, faction, eventCause);
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

        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent event = new FactionClaimEventImpl(player, faction, world, chunkPosition, eventCause);
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

        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionCreateEvent event = new FactionCreateEventImpl(player, faction, eventCause);
        return Sponge.getEventManager().post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionKickEvent(final FactionPlayer kickedPlayer, final Player kickedBy, final Faction faction)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, kickedBy)
                .add(EventContextKeys.PLAYER, kickedBy)
                .add(EventContextKeys.CREATOR, kickedBy)
                .build();

        final Cause eventCause = Cause.of(eventContext, kickedBy, faction);
        final FactionKickEvent event = new FactionKickEventImpl(kickedPlayer, kickedBy, faction, eventCause);
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

        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent.Unclaim event = new FactionUnclaimEventImpl(player, faction, world, chunkPosition, eventCause);
        return Sponge.getEventManager().post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
	public static boolean runFactionAreaEnterEvent(final MoveEntityEvent moveEntityEvent, final Player player, final Optional<Faction> enteredFaction, final Optional<Faction> leftFaction)
	{
	    final EventContext eventContext = EventContext.builder()
            .add(EventContextKeys.OWNER, player)
            .add(EventContextKeys.PLAYER, player)
            .add(EventContextKeys.CREATOR, player)
            .build();

        final Cause eventCause = Cause.of(eventContext, player, enteredFaction, leftFaction);
        final FactionAreaEnterEvent event = new FactionAreaEnterEventImpl(moveEntityEvent, player, enteredFaction, leftFaction, eventCause);
        return Sponge.getEventManager().post(event);
	}

    public static boolean runFactionDisbandEvent(final Player player, final Faction playerFaction)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, player)
                .add(EventContextKeys.PLAYER, player)
                .add(EventContextKeys.CREATOR, player)
                .build();

        final Cause cause = Cause.of(eventContext, player, playerFaction);
        final FactionDisbandEvent event = new FactionAreaEnterEventImp(player, playerFaction, cause);
        return Sponge.getEventManager().post(event);
    }
}
