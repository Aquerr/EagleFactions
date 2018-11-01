package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.UUID;

public class FactionPlayer implements IFactionPlayer
{
    private UUID uniqueId;
    private String name;

    private String factionName;
    private FactionMemberType factionRole;

    public FactionPlayer(String playerName, UUID uniqueId, String factionName, FactionMemberType factionRole)
    {
        this.name = playerName;
        this.uniqueId = uniqueId;

        this.factionName = factionName;
        this.factionRole = factionRole;
    }

    public static FactionPlayer from(User playerUser)
    {
        String factionName = "";
        FactionMemberType factionMemberType = null;
        Optional<Faction> optionalFaction = EagleFactions.getPlugin().getFactionLogic().getFactionByPlayerUUID(playerUser.getUniqueId());
        if (optionalFaction.isPresent())
        {
            factionName = optionalFaction.get().getName();
            factionMemberType = optionalFaction.get().getPlayerMemberType(playerUser.getUniqueId());
        }

        return new FactionPlayer(playerUser.getName(), playerUser.getUniqueId(), factionName, factionMemberType);
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
    public Optional<String> getFactionName()
    {
        if (this.factionName == null || this.factionName.equals(""))
        {
            return Optional.empty();
        }
        else
        {
            return Optional.of(this.factionName);
        }
    }

    @Override
    public Optional<FactionMemberType> getFactionRole()
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
