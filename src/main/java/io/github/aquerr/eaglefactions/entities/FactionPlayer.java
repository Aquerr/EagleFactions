package io.github.aquerr.eaglefactions.entities;

import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class FactionPlayer implements IFactionPlayer
{
    private UUID uniqueId;
    private String name;

    private String factionName;
    private boolean hasFaction;
    private FactionMemberType factionRole;

    public FactionPlayer(Player player)
    {
        this.uniqueId = player.getUniqueId();
        this.name = player.getName();

        this.factionName = "";
        this.hasFaction = false;
        this.factionRole = null;
    }

    public FactionPlayer(String name, UUID uniqueId)
    {
        this.name = name;
        this.uniqueId = uniqueId;

        this.factionName = "";
        this.hasFaction = false;
        this.factionRole = null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public UUID getUniqueId()
    {
        return this.uniqueId;
    }

    @Override
    public String getFactionName() throws IllegalStateException
    {
        if(!hasFaction)
            throw new IllegalStateException("Player does not have a faction");
        return this.factionName;
    }

    @Override
    public boolean hasFaction()
    {
        return this.hasFaction;
    }

    @Override
    public FactionMemberType getFactionRole() throws IllegalStateException
    {
        if(!hasFaction)
            throw new IllegalStateException("Player does not have a faction");
        return this.factionRole;
    }
}
