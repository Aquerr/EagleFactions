package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Optional;
import java.util.UUID;

//TODO: Totally new class. Not really used in Eagle Factions yet. But it will be, in the future.
public class FactionPlayerImpl implements FactionPlayer
{
    private final UUID uniqueId;
    private final String name;

    private String factionName;
    private FactionMemberType factionRole;

    private float power;
    private float maxpower;

    public FactionPlayerImpl(final String playerName, final UUID uniqueId, final String factionName, final FactionMemberType factionRole, final float power, final float maxpower)
    {
        this.name = playerName;
        this.uniqueId = uniqueId;

        this.factionName = factionName;
        this.factionRole = factionRole;

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

    public Optional<User> getUser()
    {
        final Optional<UserStorageService> userStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorageService.flatMap(storageService -> storageService.get(this.uniqueId));
    }
}
