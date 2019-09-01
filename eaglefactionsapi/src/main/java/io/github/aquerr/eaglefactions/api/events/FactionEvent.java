package io.github.aquerr.eaglefactions.api.events;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

public interface FactionEvent extends Event, Cancellable
{
    /**
     * Gets the player that triggered the event.
     * @return {@link Player} object.
     */
    Player getCreator();

    /**
     * Gets faction that this event is related to.
     * Clients can cancel this event by sending <tt>true</tt> to {@link Cancellable#setCancelled(boolean)} method.
     */
    Faction getFaction();
}
