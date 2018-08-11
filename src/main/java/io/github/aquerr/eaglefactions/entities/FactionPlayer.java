package io.github.aquerr.eaglefactions.entities;

import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class FactionPlayer implements IFactionPlayer
{
    private UUID playerUniqueId;
    private String name;

    private String factionName;
    private boolean hasFaction;
    private FactionMemberType factionRole;

    public FactionPlayer(Player player)
    {
        this.playerUniqueId = player.getUniqueId();
        this.name = player.getName();
    }

    @Override
    public String getFactionName()
    {
        if(!hasFaction)
            throw new UnsupportedOperationException("Player does not have a faction");
        return this.factionName;
    }

    @Override
    public boolean hasFaction()
    {
        return this.hasFaction;
    }

    @Override
    public FactionMemberType getFactionRole()
    {
        if(!hasFaction)
            throw new UnsupportedOperationException("Player does not have a faction");
        return this.factionRole;
    }
}
