package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.impl.AbstractEvent;

public class FactionKickEvent extends AbstractEvent
{
    private final Cause _cause;
    private final IFactionPlayer _kickedPlayer;
    private final Player _kickedBy;
    private final Faction _faction;

    /**
     * @return True if cancelled, false if not
     */
    public static boolean runEvent(IFactionPlayer kickedPlayer, Player kickedBy, Faction faction)
    {
        final EventContext eventContext = EventContext.builder()
                .add(EventContextKeys.OWNER, kickedBy)
                .add(EventContextKeys.PLAYER, kickedBy)
                .add(EventContextKeys.CREATOR, kickedBy)
                .build();

        final Cause creationEventCause = Cause.of(eventContext, kickedBy);
        final FactionKickEvent event = new FactionKickEvent(kickedPlayer, kickedBy, faction, creationEventCause);
        return Sponge.getEventManager().post(event);
    }

    public FactionKickEvent(IFactionPlayer kickedPlayer, Player kickedBy, Faction faction, Cause cause)
    {
        this._kickedPlayer = kickedPlayer;
        this._kickedBy = kickedBy;
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

    public IFactionPlayer getKickedPlayer()
    {
        return this._kickedPlayer;
    }

    public Player getKickedBy()
    {
        return this._kickedBy;
    }
}
