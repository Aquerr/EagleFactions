package io.github.aquerr.eaglefactions.common.events;

import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.impl.AbstractEvent;

public class FactionCreateEvent extends AbstractEvent
{
    private final Cause _cause;
    private final Player _creator;
    private final Faction _faction;

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runEvent(Player player, Faction faction)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, player)
                .add(EventContextKeys.PLAYER, player)
                .add(EventContextKeys.CREATOR, player)
                .build();

        final Cause creationEventCause = Cause.of(eventContext, player);
        final FactionCreateEvent event = new FactionCreateEvent(player, faction, creationEventCause);
        return Sponge.getEventManager().post(event);
    }

    public FactionCreateEvent(Player creator, Faction faction, Cause cause)
    {
        this._creator = creator;
        this._faction = faction;
        this._cause = cause;
    }

    @Override
    public Cause getCause()
    {
        return this._cause;
    }

    public Faction getFaction()
    {
        return this._faction;
    }

    public Player getCreator()
    {
        return this._creator;
    }
}
