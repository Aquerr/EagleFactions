package io.github.aquerr.eaglefactions.api.entities;

import java.util.Optional;
import java.util.UUID;

public interface IFactionPlayer
{
    String getName();

    UUID getUniqueId();

    Optional<String> getFactionName();

    Optional<FactionMemberType> getFactionRole();
}
