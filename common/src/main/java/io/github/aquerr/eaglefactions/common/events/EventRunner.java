package io.github.aquerr.eaglefactions.common.events;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.*;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
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

    public static boolean runFactionDisbandEvent(final Object source, final Faction playerFaction, final boolean forceRemovedByAdmin, final boolean removedDueToInactiviy)
    {
        // Some special code here... because DisbandEvent can also be fired by FactionsRemover.
        // TODO: Maybe it can be written better?

        EventContext.Builder eventContextBuilder = EventContext.builder();
        Cause.Builder causeBuilder = Cause.builder();
        if (source instanceof Player)
        {
            final Player player = (Player)source;
            eventContextBuilder.add(EventContextKeys.OWNER, player)
                    .add(EventContextKeys.PLAYER, player)
                    .add(EventContextKeys.CREATOR, player)
                    .build();
            causeBuilder.append(player).append(playerFaction);
        }
        else
        {
            causeBuilder.append(EagleFactionsPlugin.getPlugin()).append(playerFaction);
        }

        EventContext eventContext = eventContextBuilder.build();
        Cause cause = causeBuilder.build(eventContext);

        FactionDisbandEvent event;

        if (source instanceof Player)
        {
            event = new FactionDisbandEventImpl((Player)source, playerFaction, forceRemovedByAdmin, removedDueToInactiviy, cause);
        }
        else
        {
            event = new FactionDisbandEventImpl(null, playerFaction, forceRemovedByAdmin, removedDueToInactiviy, cause);
        }
        return Sponge.getEventManager().post(event);
    }
}
