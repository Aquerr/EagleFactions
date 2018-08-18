package io.github.aquerr.eaglefactions.entities;

import java.util.UUID;

public interface IFactionPlayer
{
    String getName();

    UUID getUniqueId();

    String getFactionName() throws IllegalStateException;

    boolean hasFaction();

    FactionMemberType getFactionRole() throws IllegalStateException;
}
