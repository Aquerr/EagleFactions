package io.github.aquerr.eaglefactions.common.managers;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.IPlayerManager;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.common.storage.StorageManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerManager implements IPlayerManager
{
    private static PlayerManager INSTANCE = null;

    private final ConfigFields _configFields;
    private final StorageManager storageManager;

    private UserStorageService userStorageService;

    private PlayerManager(final EagleFactions plugin)
    {
        INSTANCE = this;
        _configFields = plugin.getConfiguration().getConfigFields();
        storageManager = StorageManager.getInstance(plugin);

        Optional<UserStorageService> optionalUserStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
        optionalUserStorageService.ifPresent(x -> userStorageService = x);
    }

    public static PlayerManager getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new PlayerManager(eagleFactions);
        else return INSTANCE;
    }

    public boolean addPlayer(UUID playerUUID, String playerName)
    {
        return storageManager.addPlayer(playerUUID, playerName, _configFields.getStartingPower(), _configFields.getGlobalMaxPower());
    }

    public float getPlayerPower(UUID playerUUID)
    {
        return storageManager.getPlayerPower(playerUUID);
    }

    public boolean setPlayerPower(UUID playerUUID, float power)
    {
        return storageManager.setPlayerPower(playerUUID, power);
    }

    public float getPlayerMaxPower(UUID playerUUID)
    {
        return storageManager.getPlayerMaxPower(playerUUID);
    }

    public boolean setPlayerMaxPower(UUID playerUUID, float maxpower)
    {
        return storageManager.setPlayerMaxPower(playerUUID, maxpower);
    }

    public Optional<String> getPlayerName(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        if(oUser.isPresent())
            return Optional.of(oUser.get().getName());
        else
        {
            return getLastKnownPlayerName(playerUUID);
        }
    }

    private Optional<String> getLastKnownPlayerName(UUID playerUUID)
    {
        String playerName = storageManager.getPlayerName(playerUUID);
        if(playerName.equals(""))
            return Optional.empty();
        return Optional.of(playerName);
    }

    public Optional<Player> getPlayer(UUID playerUUID)
    {
        final Optional<User> oUser = getUser(playerUUID);
        if(!oUser.isPresent())
            return Optional.empty();
        return oUser.get().getPlayer();
    }

    private Optional<User> getUser(UUID playerUUID)
    {
        Optional<User> oUser = userStorageService.get(playerUUID);
        return oUser;
    }

    public boolean isPlayerOnline(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);
        return oUser.map(User::isOnline).orElse(false);
    }

    public Set<String> getServerPlayerNames()
    {
        return storageManager.getServerPlayerNames();
    }

    public void setDeathInWarZone(UUID playerUUID, boolean didDieInWarZone)
    {
        storageManager.setDeathInWarzone(playerUUID, didDieInWarZone);
    }

    public boolean lastDeathAtWarZone(UUID playerUUID)
    {
       return storageManager.getLastDeathInWarzone(playerUUID);
    }

    public boolean checkIfPlayerExists(UUID playerUUID, String playerName)
    {
        return storageManager.checkIfPlayerExists(playerUUID, playerName);
    }

    @Nullable
    public FactionMemberType getFactionMemberType(UUID playerUUID, Faction faction)
    {
        if(faction.getLeader() != null && faction.getLeader().equals(playerUUID))
        {
            return FactionMemberType.LEADER;
        }
        else if(faction.getMembers().contains(playerUUID))
        {
            return FactionMemberType.MEMBER;
        }
        else if(faction.getOfficers().contains(playerUUID))
        {
            return FactionMemberType.OFFICER;
        }
        else if(faction.getRecruits().contains(playerUUID))
        {
            return FactionMemberType.RECRUIT;
        }
//        else if(faction.getAlliances().contains(factionPlayer.getUniqueId().toString()))
//        {
//            return FactionMemberType.ALLY;
//        }

        return null;
    }

    public Set<IFactionPlayer> getServerPlayers()
    {
        return storageManager.getServerPlayers();
    }

    public void updatePlayerName(UUID playerUUID, String playerName)
    {
        this.storageManager.updatePlayerName(playerUUID, playerName);
    }
}
