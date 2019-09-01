package io.github.aquerr.eaglefactions.api.events;

import org.spongepowered.api.entity.living.player.Player;

public interface FactionInviteEvent extends FactionEvent
{
    /**
     * @return {@link Player} who has been invited to the faction.
     */
    Player getInvitedPlayer();
}
