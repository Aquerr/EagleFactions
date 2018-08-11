package io.github.aquerr.eaglefactions.entities;

public interface IFactionPlayer
{
    String getFactionName();

    boolean hasFaction();

    FactionMemberType getFactionRole();
}
