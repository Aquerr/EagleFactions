package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Optional;
import java.util.UUID;

public class FactionPlayerImpl implements FactionPlayer
{
    private final UUID uniqueId;
    private final String name;

    private Faction faction;
    private FactionMemberType factionRole;

    private boolean diedInWarZone;

    private float power;
    private float maxpower;

    public FactionPlayerImpl(final String playerName, final UUID uniqueId, final Faction faction, final float power, final float maxpower, final FactionMemberType factionRole, final boolean diedInWarZone)
    {
        this.name = playerName;
        this.uniqueId = uniqueId;

        this.faction = faction;

        if (factionRole == null)
            this.factionRole = FactionMemberType.NONE;
        else this.factionRole = factionRole;

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
    public UUID getUniqueId()
    {
        return this.uniqueId;
    }

    @Override
    public Optional<Faction> getFaction()
    {
        return Optional.ofNullable(this.faction);
    }

    @Override
    public FactionMemberType getFactionRole()
    {
        return this.factionRole;
    }

    @Override
    public boolean isOnline()
    {
        final Optional<User> optionalUser = getUser();
        return optionalUser.map(User::isOnline).orElse(false);
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
    public Optional<User> getUser()
    {
        final Optional<UserStorageService> userStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorageService.flatMap(storageService -> storageService.get(this.uniqueId));
    }

    @Override
    public boolean diedInWarZone()
    {
        return this.diedInWarZone;
    }
}
