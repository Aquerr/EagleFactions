package io.github.aquerr.eaglefactions.entities;

import org.spongepowered.api.entity.living.player.Player;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public class FactionPlayer implements IFactionPlayer
{
    private UUID uniqueId;
    private String name;

    private String factionName;
    private FactionMemberType factionRole;

    public FactionPlayer(Player player)
    {
        this.uniqueId = player.getUniqueId();
        this.name = player.getName();

        this.factionName = "";
        this.factionRole = null;
    }

    public FactionPlayer(String name, UUID uniqueId)
    {
        this.name = name;
        this.uniqueId = uniqueId;

        this.factionName = "";
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
    public Optional<String> getFactionName() throws IllegalStateException
    {
        if (this.factionName.equals(""))
        {
            return Optional.empty();
        }
        else
        {
            return Optional.of(this.factionName);
        }
    }

    @Override
    public Optional<FactionMemberType> getFactionRole() throws IllegalStateException
    {
        if (this.factionRole == null)
        {
            return Optional.empty();
        }
        else
        {
            return Optional.of(this.factionRole);
        }
    }
}
