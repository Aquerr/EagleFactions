package io.github.aquerr.eaglefactions.entities;

import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.UUID;

public interface IFactionPlayer
{
    String getName();

    UUID getUniqueId();

    Optional<String> getFactionName();

    Optional<FactionMemberType> getFactionRole();
}
