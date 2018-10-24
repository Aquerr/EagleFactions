package io.github.aquerr.eaglefactions.entities;

import java.util.Optional;
import java.util.UUID;

public interface IFactionPlayer
{
    String getName();

    UUID getUniqueId();

    Optional<String> getFactionName() throws IllegalStateException;

    Optional<FactionMemberType> getFactionRole() throws IllegalStateException;
}
