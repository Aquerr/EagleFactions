package io.github.aquerr.eaglefactions.model;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.util.ServerUtils;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.UUID;

public class FactionPlayerImpl implements FactionPlayer
{
    private final UUID uniqueId;
    private final String name;

    private String factionName;

    private boolean diedInWarZone;

    private float power;
    private float maxpower;

    public FactionPlayerImpl(final String playerName, final UUID uniqueId, final String factionName, final float power, final float maxpower, final boolean diedInWarZone)
    {
        this.name = playerName;
        this.uniqueId = uniqueId;

        if (StringUtils.isBlank(factionName))
            this.factionName = null;
        else this.factionName = factionName;

        this.diedInWarZone = diedInWarZone;

        this.power = power;
        this.maxpower = maxpower;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Optional<String> getFactionName()
    {
        return Optional.ofNullable(this.factionName);
    }

    @Override
    public UUID getUniqueId()
    {
        return this.uniqueId;
    }

    @Override
    public Optional<Faction> getFaction()
    {
        return getFactionName()
                .filter(StringUtils::isNotBlank)
                .map(EagleFactionsPlugin.getPlugin().getFactionManager()::getFactionByName);
    }

    @Override
    public FactionMemberType getFactionRole()
    {
        return this.getFaction().map(faction -> faction.getPlayerMemberType(this.uniqueId)).orElse(FactionMemberType.NONE);
    }

    @Override
    public boolean isOnline()
    {
        return ServerUtils.getServer().getPlayerList().getPlayer(this.uniqueId) != null;
    }

    @Override
    public Optional<ServerPlayer> getServerPlayer()
    {
        return Optional.ofNullable(ServerUtils.getServer().getPlayerList().getPlayer(this.uniqueId));
    }

    @Override
    public float getPower()
    {
        return this.power;
    }

    @Override
    public float getMaxPower()
    {
        return this.maxpower;
    }

    @Override
    public boolean diedInWarZone()
    {
        return this.diedInWarZone;
    }
}