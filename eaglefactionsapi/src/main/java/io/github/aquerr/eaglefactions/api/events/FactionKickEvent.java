package io.github.aquerr.eaglefactions.api.events;

import io.github.aquerr.eaglefactions.api.entities.IFactionPlayer;

public interface FactionKickEvent extends FactionEvent
{
    /**
     * @return the {@link IFactionPlayer} who has been kicked from the faction.
     */
    IFactionPlayer getKickedPlayer();
}
