package io.github.aquerr.eaglefactions.messaging.placeholder;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public interface ParsingObjectType
{
    ParsingObjectType FACTION_CLASS = () -> Faction.class;
    ParsingObjectType SERVER_PLAYER_CLASS = () -> ServerPlayer.class;

    Class<?> objectTypeProvider();
}
