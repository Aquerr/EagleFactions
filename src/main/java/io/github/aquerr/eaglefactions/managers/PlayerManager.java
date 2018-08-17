package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.storage.IPlayerStorage;
import io.github.aquerr.eaglefactions.storage.hocon.HOCONPlayerStorage;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerManager
{
    private ConfigFields _configFields;
    private UserStorageService userStorageService;
    private IPlayerStorage _playerStorage;

    public PlayerManager(EagleFactions plugin)
    {
        _configFields = plugin.getConfiguration().getConfigFileds();
        _playerStorage = new HOCONPlayerStorage(plugin.getConfigDir());

        Optional<UserStorageService> optionalUserStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
        optionalUserStorageService.ifPresent(userStorageService1 -> userStorageService = userStorageService1);
    }

    public boolean addPlayer(UUID playerUUID, String playerName)
    {
        return _playerStorage.addPlayer(playerUUID, playerName, _configFields.getStartingPower(), _configFields.getGlobalMaxPower());
    }

    public BigDecimal getPlayerPower(UUID playerUUID)
    {
        return _playerStorage.getPlayerPower(playerUUID);
    }

    public boolean setPlayerPower(UUID playerUUID, BigDecimal power)
    {
        return _playerStorage.setPlayerPower(playerUUID, power);
    }

    public BigDecimal getPlayerMaxPower(UUID playerUUID)
    {
        return _playerStorage.getPlayerMaxPower(playerUUID);
    }

    public boolean setPlayerMaxPower(UUID playerUUID, BigDecimal maxpower)
    {
        return _playerStorage.setPlayerMaxPower(playerUUID, maxpower);
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
        String playerName = _playerStorage.getPlayerName(playerUUID);
        if(playerName.equals(""))
            return Optional.empty();
        return Optional.of(playerName);
    }

    public Optional<Player> getPlayer(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

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
        return _playerStorage.getServerPlayerNames();
    }

    public void setDeathInWarZone(UUID playerUUID, boolean didDieInWarZone)
    {
        _playerStorage.setDeathInWarzone(playerUUID, didDieInWarZone);
    }

    public boolean lastDeathAtWarZone(UUID playerUUID)
    {
       return _playerStorage.getLastDeathInWarzone(playerUUID);
    }

    public boolean checkIfPlayerExists(UUID playerUUID, String playerName)
    {
        return _playerStorage.checkIfPlayerExists(playerUUID, playerName);
    }

    @Nullable
    public FactionMemberType getFactionMemberType(Player factionPlayer, Faction faction)
    {
        if(faction.getLeader() != null && faction.getLeader().equals(factionPlayer.getUniqueId()))
        {
            return FactionMemberType.LEADER;
        }
        else if(faction.getMembers().contains(factionPlayer.getUniqueId()))
        {
            return FactionMemberType.MEMBER;
        }
        else if(faction.getOfficers().contains(factionPlayer.getUniqueId()))
        {
            return FactionMemberType.OFFICER;
        }
        else if(faction.getRecruits().contains(factionPlayer.getUniqueId()))
        {
            return FactionMemberType.RECRUIT;
        }
        else if(faction.getAlliances().contains(factionPlayer.getUniqueId().toString()))
        {
            return FactionMemberType.ALLY;
        }

        return null;
    }

    public Set<IFactionPlayer> getServerPlayers()
    {
        return _playerStorage.getServerPlayers();
    }
}
