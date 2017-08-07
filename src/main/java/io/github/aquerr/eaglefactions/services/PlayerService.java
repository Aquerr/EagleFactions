package io.github.aquerr.eaglefactions.services;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerService
{
    public static Optional<String> getPlayerName(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        return Optional.of(oUser.get().getName());
    }

    public static Optional<Player> getPlayer(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        return oUser.get().getPlayer();
    }

    private static Optional<User> getUser(UUID playerUUID)
    {
        UserStorageService userStorageService = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> oUser = userStorageService.get(playerUUID);

        if(oUser.isPresent())
        {
            return oUser;
        }
        else return Optional.empty();
    }

    public static boolean isPlayerOnline(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        if(oUser.isPresent())
        {
            return oUser.get().isOnline();
        }
        else return false;
    }
}
