package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.events.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionLeaveEvent event = new FactionLeaveEventImpl.Pre(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionLeaveEventPost(final Player player, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionLeaveEvent event = new FactionLeaveEventImpl.Post(player, faction, eventCause);
        return eventManager.post(event);
    }

    public static boolean runFactionJoinEventPre(final Player player, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionJoinEvent event = new FactionJoinEventImpl.Pre(player, faction, eventCause);
        return eventManager.post(event);
    }

    public static boolean runFactionJoinEventPost(final Player player, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionJoinEvent event = new FactionJoinEventImpl.Post(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionChestEventPre(final Player player, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionChestEvent event = new FactionChestEventImpl.Pre(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionChestEventPost(final Player player, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionChestEvent event = new FactionChestEventImpl.Post(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionClaimEventPre(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent event = new FactionClaimEventImpl.Pre(player, faction, world, chunkPosition, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionClaimEventPost(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent event = new FactionClaimEventImpl.Post(player, faction, world, chunkPosition, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionCreateEventPre(final Player player, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionCreateEvent event = new FactionCreateEventImpl.Pre(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionCreateEventPost(final Player player, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionCreateEvent event = new FactionCreateEventImpl.Post(player, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionKickEventPre(final FactionPlayer kickedPlayer, final Player kickedBy, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(kickedBy).build();
        final Cause eventCause = Cause.of(eventContext, kickedBy, faction);
        final FactionKickEvent event = new FactionKickEventImpl.Pre(kickedPlayer, kickedBy, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionKickEventPost(final FactionPlayer kickedPlayer, final Player kickedBy, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(kickedBy).build();
        final Cause eventCause = Cause.of(eventContext, kickedBy, faction);
        final FactionKickEvent event = new FactionKickEventImpl.Post(kickedPlayer, kickedBy, faction, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionUnclaimEventPre(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent.Unclaim event = new FactionUnclaimEventImpl.Pre(player, faction, world, chunkPosition, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionUnclaimEventPost(final Player player, final Faction faction, final World world, final Vector3i chunkPosition)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, faction);
        final FactionClaimEvent.Unclaim event = new FactionUnclaimEventImpl.Post(player, faction, world, chunkPosition, eventCause);
        return eventManager.post(event);
    }

    /**
     * @return True if cancelled, false if not
     */
	public static boolean runFactionAreaEnterEventPre(final MoveEntityEvent moveEntityEvent, final Player player, final Optional<Faction> enteredFaction, final Optional<Faction> leftFaction)
	{
	    final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, enteredFaction, leftFaction);
        final FactionAreaEnterEvent event = new FactionAreaEnterEventImpl.Pre(moveEntityEvent, player, enteredFaction, leftFaction, eventCause);
        return eventManager.post(event);
	}

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runFactionAreaEnterEventPost(final MoveEntityEvent moveEntityEvent, final Player player, final Optional<Faction> enteredFaction, final Optional<Faction> leftFaction)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause eventCause = Cause.of(eventContext, player, enteredFaction, leftFaction);
        final FactionAreaEnterEvent event = new FactionAreaEnterEventImpl.Post(moveEntityEvent, player, enteredFaction, leftFaction, eventCause);
        return eventManager.post(event);
    }


    public static boolean runFactionRenameEventPre(final Player player, final Faction faction, final String newName)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionRenameEvent event = new FactionRenameEventImpl.Pre(player, faction, newName, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionRenameEventPost(final Player player, final Faction faction, final String newName)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionRenameEvent event = new FactionRenameEventImpl.Post(player, faction, newName, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionDisbandEventPre(final Object source, final Faction playerFaction, final boolean forceRemovedByAdmin, final boolean removedDueToInactivity)
    {
        // Some special code here... because DisbandEvent can also be fired by FactionsRemover.
        // TODO: Maybe it can be written better?

        EventContext.Builder eventContextBuilder = EventContext.builder();
        final Cause.Builder causeBuilder = Cause.builder();
        if (source instanceof Player)
        {
            final Player player = (Player)source;
            eventContextBuilder = getEventContextForPlayer(player);
            causeBuilder.append(player).append(playerFaction);
        }
        else
        {
            causeBuilder.append(EagleFactionsPlugin.getPlugin()).append(playerFaction);
        }

        final EventContext eventContext = eventContextBuilder.build();
        final Cause cause = causeBuilder.build(eventContext);

        FactionDisbandEvent event;

        if (source instanceof Player)
        {
            event = new FactionDisbandEventImpl.Pre((Player)source, playerFaction, forceRemovedByAdmin, removedDueToInactivity, cause);
        }
        else
        {
            event = new FactionDisbandEventImpl.Pre(null, playerFaction, forceRemovedByAdmin, removedDueToInactivity, cause);
        }
        return eventManager.post(event);
    }

    public static boolean runFactionDisbandEventPost(final Object source, final Faction playerFaction, final boolean forceRemovedByAdmin, final boolean removedDueToInactiviy)
    {
        // Some special code here... because DisbandEvent can also be fired by FactionsRemover.
        // TODO: Maybe it can be written better?

        EventContext.Builder eventContextBuilder = EventContext.builder();
        final Cause.Builder causeBuilder = Cause.builder();
        if (source instanceof Player)
        {
            final Player player = (Player)source;
            eventContextBuilder = getEventContextForPlayer(player);
            causeBuilder.append(player).append(playerFaction);
        }
        else
        {
            causeBuilder.append(EagleFactionsPlugin.getPlugin()).append(playerFaction);
        }

        final EventContext eventContext = eventContextBuilder.build();
        final Cause cause = causeBuilder.build(eventContext);

        FactionDisbandEvent event;

        if (source instanceof Player)
        {
            event = new FactionDisbandEventImpl.Post((Player)source, playerFaction, forceRemovedByAdmin, removedDueToInactiviy, cause);
        }
        else
        {
            event = new FactionDisbandEventImpl.Post(null, playerFaction, forceRemovedByAdmin, removedDueToInactiviy, cause);
        }
        return eventManager.post(event);
    }

    public static boolean runFactionDemoteEventPre(@Nullable final Player demotedBy, final FactionPlayer demotedPlayer, final Faction faction)
    {
        final EventContext eventContext = Optional.ofNullable(demotedBy)
                .map(EventRunner::getEventContextForPlayer)
                .map(EventContext.Builder::build)
                .orElse(defaultEventContext().build());

        List<Object> causes = new ArrayList<>();
        if (demotedBy != null)
        {
            causes.add(demotedBy);
        }
        causes.add(faction);
        causes.add(demotedPlayer);
        final Cause cause = Cause.of(eventContext, causes);
        final FactionDemoteEvent.Pre event = new FactionDemoteEventImpl.Pre(faction, demotedBy, demotedPlayer, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionDemoteEventPost(@Nullable final Player demotedBy, final FactionPlayer demotedPlayer, final FactionMemberType demotedTo, final Faction faction)
    {
        final EventContext eventContext = Optional.ofNullable(demotedBy)
                .map(EventRunner::getEventContextForPlayer)
                .map(EventContext.Builder::build)
                .orElse(defaultEventContext().build());

        List<Object> causes = new ArrayList<>();
        if (demotedBy != null)
        {
            causes.add(demotedBy);
        }
        causes.add(faction);
        causes.add(demotedPlayer);
        final Cause cause = Cause.of(eventContext, causes);
        final FactionDemoteEvent event = new FactionDemoteEventImpl.Post(faction, demotedBy, demotedPlayer, demotedTo, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionPromoteEventPre(@Nullable final Player promotedBy, final FactionPlayer promotedPlayer, final Faction faction)
    {
        final EventContext eventContext = Optional.ofNullable(promotedBy)
                .map(EventRunner::getEventContextForPlayer)
                .map(EventContext.Builder::build)
                .orElse(defaultEventContext().build());

        List<Object> causes = new ArrayList<>();
        if (promotedBy != null)
        {
            causes.add(promotedBy);
        }
        causes.add(faction);
        causes.add(promotedPlayer);
        final Cause cause = Cause.of(eventContext, causes);
        final FactionPromoteEvent.Pre event = new FactionPromoteEventImpl.Pre(promotedBy, promotedPlayer, faction, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionPromoteEventPost(@Nullable final Player promotedBy, final FactionPlayer promotedPlayer, final FactionMemberType promotedToRank, final Faction faction)
    {
        final EventContext eventContext = Optional.ofNullable(promotedBy)
                .map(EventRunner::getEventContextForPlayer)
                .map(EventContext.Builder::build)
                .orElse(defaultEventContext().build());

        List<Object> causes = new ArrayList<>();
        if (promotedBy != null)
        {
            causes.add(promotedBy);
        }
        causes.add(faction);
        causes.add(promotedPlayer);
        final Cause cause = Cause.of(eventContext, causes);
        final FactionPromoteEvent event = new FactionPromoteEventImpl.Post(promotedBy, promotedPlayer, faction, promotedToRank, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionInviteEventPre(final Player inviter, final Player invited, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(inviter).build();
        final Cause cause = Cause.of(eventContext, inviter, faction, invited);
        final FactionInviteEvent event = new FactionInviteEventImpl.Pre(inviter, invited, faction, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionInviteEventPost(final Player inviter, final Player invited, final Faction faction)
    {
        final EventContext eventContext = getEventContextForPlayer(inviter).build();
        final Cause cause = Cause.of(eventContext, inviter, faction, invited);
        final FactionInviteEvent event = new FactionInviteEventImpl.Post(inviter, invited, faction, cause);
        return eventManager.post(event);
    }

    public static boolean runFactionTagUpdateEventPre(Player player, Faction faction, TextComponent oldTag, String newTag)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionTagUpdateEvent.Pre event = new FactionTagUpdateEventImpl.Pre(player, faction, cause, oldTag, newTag);
        return eventManager.post(event);
    }

    public static boolean runFactionTagUpdateEventPost(Player player, Faction faction, TextComponent oldTag, String newTag)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionTagUpdateEvent.Post event = new FactionTagUpdateEventImpl.Post(player, faction, cause, oldTag, newTag);
        return eventManager.post(event);
    }

    public static boolean runFactionTagColorUpdateEventPre(Player player, Faction faction, NamedTextColor oldColor, NamedTextColor newColor)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionTagColorUpdateEvent.Pre event = new FactionTagColorUpdateEventImpl.Pre(player, faction, cause, oldColor, newColor);
        return eventManager.post(event);
    }

    public static boolean runFactionTagColorUpdateEventPost(Player player, Faction faction, NamedTextColor oldColor, NamedTextColor newColor)
    {
        final EventContext eventContext = getEventContextForPlayer(player).build();
        final Cause cause = Cause.of(eventContext, player, faction);
        final FactionTagColorUpdateEvent.Post event = new FactionTagColorUpdateEventImpl.Post(player, faction, cause, oldColor, newColor);
        return eventManager.post(event);
    }

    private static EventContext.Builder getEventContextForPlayer(final Player player)
    {
        return EventContext.builder()
                .add(EventContextKeys.AUDIENCE, player)
                .add(EventContextKeys.PLAYER, player)
                .add(EventContextKeys.CREATOR, player.uniqueId())
                .add(EventContextKeys.PLUGIN, EagleFactionsPlugin.getPlugin().getPluginContainer());
    }

    private static EventContext.Builder defaultEventContext() {
        return EventContext.builder()
                .add(EventContextKeys.PLUGIN, EagleFactionsPlugin.getPlugin().getPluginContainer());
    }
}
