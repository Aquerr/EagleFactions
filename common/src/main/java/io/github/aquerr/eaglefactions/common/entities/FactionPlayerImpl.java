package io.github.aquerr.eaglefactions.common.entities;

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

    private String factionName;
    private FactionMemberType factionRole;

    private boolean diedInWarZone;

    private float power;
    private float maxpower;

    public FactionPlayerImpl(final String playerName, final UUID uniqueId, final String factionName, final float power, final float maxpower, final FactionMemberType factionRole, final boolean diedInWarZone)
    {
        this.name = playerName;
        this.uniqueId = uniqueId;

        this.factionName = factionName;

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
