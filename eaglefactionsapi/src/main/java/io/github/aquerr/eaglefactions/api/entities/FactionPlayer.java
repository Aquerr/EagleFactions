package io.github.aquerr.eaglefactions.api.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.config.ConfigFields;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.UUID;

public class FactionPlayer implements IFactionPlayer
{
    private UUID uniqueId;
    private String name;

    private String factionName;
    private FactionMemberType factionRole;

    private float power;
    private float maxpower;

    public FactionPlayer(String playerName, UUID uniqueId, String factionName, FactionMemberType factionRole, float power, float maxpower)
    {
        this.name = playerName;
        this.uniqueId = uniqueId;

        this.factionName = factionName;
        this.factionRole = factionRole;

        this.power = power;
        this.maxpower = maxpower;
    }

    public static FactionPlayer from(User playerUser)
    {
        String factionName = "";
        FactionMemberType factionMemberType = null;
        ConfigFields configFields = EagleFactionsPlugin.getPlugin().getConfiguration().getConfigFields();
        Optional<Faction> optionalFaction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByPlayerUUID(playerUser.getUniqueId());
        if (optionalFaction.isPresent())
        {
            factionName = optionalFaction.get().getName();
            factionMemberType = optionalFaction.get().getPlayerMemberType(playerUser.getUniqueId());
        }

        return new FactionPlayer(playerUser.getName(), playerUser.getUniqueId(), factionName, factionMemberType, configFields.getStartingPower(),  configFields.getGlobalMaxPower());
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
