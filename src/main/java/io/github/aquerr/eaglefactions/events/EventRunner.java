package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.FactionAreaEnterEvent;
import io.github.aquerr.eaglefactions.api.events.FactionChestEvent;
import io.github.aquerr.eaglefactions.api.events.FactionClaimEvent;
import io.github.aquerr.eaglefactions.api.events.FactionCreateEvent;
import io.github.aquerr.eaglefactions.api.events.FactionDisbandEvent;
import io.github.aquerr.eaglefactions.api.events.FactionInviteEvent;
import io.github.aquerr.eaglefactions.api.events.FactionJoinEvent;
import io.github.aquerr.eaglefactions.api.events.FactionKickEvent;
import io.github.aquerr.eaglefactions.api.events.FactionLeaderChangeEvent;
import io.github.aquerr.eaglefactions.api.events.FactionLeaveEvent;
import io.github.aquerr.eaglefactions.api.events.FactionRenameEvent;
import io.github.aquerr.eaglefactions.api.events.FactionTagColorUpdateEvent;
import io.github.aquerr.eaglefactions.api.events.FactionTagUpdateEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A util class used for running Eagle Factions events.
 */
public final class EventRunner
{
    private static EventManager eventManager;

    public static void init(EventManager eventManagerParam)
    {
        eventManager = eventManagerParam;
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionLeaveEventPre(final Player player, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionLeaveEvent event = new FactionLeaveEventImpl.Pre(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionLeaveEventPost(final Player player, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionLeaveEvent event = new FactionLeaveEventImpl.Post(player, faction, eventCause);
        return eventManager.post(event);
    }

    public static boolean runFactionJoinEventPre(final Player player, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionJoinEvent event = new FactionJoinEventImpl.Pre(player, faction, eventCause);
        return eventManager.post(event);
    }

    public static boolean runFactionJoinEventPost(final Player player, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionJoinEvent event = new FactionJoinEventImpl.Post(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionChestEventPre(final Player player, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionChestEvent event = new FactionChestEventImpl.Pre(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionChestEventPost(final Player player, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionChestEvent event = new FactionChestEventImpl.Post(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionClaimEventPre(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent event = new FactionClaimEventImpl.Pre(player, faction, world, chunkPosition, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionClaimEventPost(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent event = new FactionClaimEventImpl.Post(player, faction, world, chunkPosition, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionCreateEventPre(@Nullable final Player player, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, Stream.of(player, faction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        final FactionCreateEvent event = new FactionCreateEventImpl.Pre(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionCreateEventPost(@Nullable final Player player, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, Stream.of(player, faction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        final FactionCreateEvent event = new FactionCreateEventImpl.Post(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionKickEventPre(final FactionPlayer kickedPlayer, final Player kickedBy, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(kickedBy).build();
        final Cause eventCause = Cause.of(eventContext, kickedBy, faction);
        final FactionKickEvent event = new FactionKickEventImpl.Pre(kickedPlayer, kickedBy, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionKickEventPost(final FactionPlayer kickedPlayer, final Player kickedBy, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(kickedBy).build();
        final Cause eventCause = Cause.of(eventContext, kickedBy, faction);
        final FactionKickEvent event = new FactionKickEventImpl.Post(kickedPlayer, kickedBy, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionUnclaimEventPre(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent.Unclaim event = new FactionUnclaimEventImpl.Pre(player, faction, world, chunkPosition, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionUnclaimEventPost(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent.Unclaim event = new FactionUnclaimEventImpl.Post(player, faction, world, chunkPosition, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
	public static boolean runFactionAreaEnterEventPre(final MoveEntityEvent moveEntityEvent, final Player player, final Optional<Faction> enteredFaction, final Optional<Faction> leftFaction)
	{
	    final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, enteredFaction, leftFaction);
        final FactionAreaEnterEvent event = new FactionAreaEnterEventImpl.Pre(moveEntityEvent, player, enteredFaction, leftFaction, eventCause);
        return eventManager.post(event);
	}

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionAreaEnterEventPost(final MoveEntityEvent moveEntityEvent, final Player player, final Optional<Faction> enteredFaction, final Optional<Faction> leftFaction)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause eventCause = Cause.of(eventContext, player, enteredFaction, leftFaction);
        final FactionAreaEnterEvent event = new FactionAreaEnterEventImpl.Post(moveEntityEvent, player, enteredFaction, leftFaction, eventCause);
        return eventManager.post(event);
    }


    public static boolean runFactionRenameEventPre(final Player player, final Faction faction, final String newName)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionRenameEvent event = new FactionRenameEventImpl.Pre(player, faction, newName, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionRenameEventPost(final Player player, final Faction faction, final String newName)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionRenameEvent event = new FactionRenameEventImpl.Post(player, faction, newName, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionDisbandEventPre(@Nullable final Player player, final Faction playerFaction, final boolean forceRemovedByAdmin, final boolean removedDueToInactivity)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause cause = Cause.of(eventContext, Stream.of(player == null ? EagleFactionsPlugin.getPlugin() : player, playerFaction));
        FactionDisbandEvent event = new FactionDisbandEventImpl.Pre(player, playerFaction, forceRemovedByAdmin, removedDueToInactivity, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionDisbandEventPost(@Nullable final Player player, final Faction playerFaction, final boolean forceRemovedByAdmin, final boolean removedDueToInactivity)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause cause = Cause.of(eventContext, Stream.of(player == null ? EagleFactionsPlugin.getPlugin() : player, playerFaction));
        FactionDisbandEvent event = new FactionDisbandEventImpl.Post(player, playerFaction, forceRemovedByAdmin, removedDueToInactivity, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionLeaderChangeEventPre(@Nullable final Player promotedBy,
                                                         @Nullable final FactionPlayer newLeader,
                                                         final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(promotedBy).build();
        final Cause cause = Cause.of(eventContext, Stream.of(promotedBy, newLeader)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        final FactionLeaderChangeEvent.Pre event = new FactionLeaderChangeEventImpl.Pre(promotedBy, newLeader, faction, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionLeaderChangeEventPost(@Nullable final Player promotedBy,
                                                          @Nullable final FactionPlayer newLeader,
                                                          final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(promotedBy).build();
        final Cause cause = Cause.of(eventContext, Stream.of(promotedBy, newLeader)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        final FactionLeaderChangeEvent.Post event = new FactionLeaderChangeEventImpl.Post(promotedBy, newLeader, faction, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionInviteEventPre(final Player inviter, final Player invited, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(inviter).build();
        final Cause cause = Cause.of(eventContext, inviter, faction, invited);
        final FactionInviteEvent event = new FactionInviteEventImpl.Pre(inviter, invited, faction, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionInviteEventPost(final Player inviter, final Player invited, final Faction faction)
    {
        final EventContext eventContext = prepareEventContext(inviter).build();
        final Cause cause = Cause.of(eventContext, inviter, faction, invited);
        final FactionInviteEvent event = new FactionInviteEventImpl.Post(inviter, invited, faction, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionTagUpdateEventPre(Player player, Faction faction, TextComponent oldTag, String newTag)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionTagUpdateEvent.Pre event = new FactionTagUpdateEventImpl.Pre(player, faction, cause, oldTag, newTag);
        return eventManager.post(event);
    }

    public static boolean runFactionTagUpdateEventPost(Player player, Faction faction, TextComponent oldTag, String newTag)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionTagUpdateEvent.Post event = new FactionTagUpdateEventImpl.Post(player, faction, cause, oldTag, newTag);
        return eventManager.post(event);
    }

    public static boolean runFactionTagColorUpdateEventPre(Player player, Faction faction, NamedTextColor oldColor, NamedTextColor newColor)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionTagColorUpdateEvent.Pre event = new FactionTagColorUpdateEventImpl.Pre(player, faction, cause, oldColor, newColor);
        return eventManager.post(event);
    }

    public static boolean runFactionTagColorUpdateEventPost(Player player, Faction faction, NamedTextColor oldColor, NamedTextColor newColor)
    {
        final EventContext eventContext = prepareEventContext(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionTagColorUpdateEvent.Post event = new FactionTagColorUpdateEventImpl.Post(player, faction, cause, oldColor, newColor);
        return eventManager.post(event);
    }

    private static EventContext.Builder prepareEventContext(@Nullable final Player player)
    {
        if (player != null)
        {
            return EventContext.builder()
                    .add(EventContextKeys.AUDIENCE, player)
                    .add(EventContextKeys.PLAYER, player)
                    .add(EventContextKeys.CREATOR, player.uniqueId())
                    .add(EventContextKeys.PLUGIN, EagleFactionsPlugin.getPlugin().getPluginContainer());
        }
        return defaultEventContext();
    }

    private static EventContext.Builder defaultEventContext() {
        return EventContext.builder()
                .add(EventContextKeys.PLUGIN, EagleFactionsPlugin.getPlugin().getPluginContainer());
    }

    private EventRunner()
    {

    }
}
