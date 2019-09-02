package io.github.aquerr.eaglefactions.common.managers;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.IPlayerManager;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.common.storage.StorageManagerImpl;
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

    private final EagleFactions plugin;

    private final ConfigFields configFields;
    private final StorageManagerImpl storageManager;

    private UserStorageService userStorageService;

    private PlayerManager(final EagleFactions plugin)
    {
        INSTANCE = this;
        this.plugin = plugin;
        this.configFields = plugin.getConfiguration().getConfigFields();
        this.storageManager = StorageManagerImpl.getInstance(plugin);

        Optional<UserStorageService> optionalUserStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
        optionalUserStorageService.ifPresent(x -> userStorageService = x);
    }

    public static PlayerManager getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new PlayerManager(eagleFactions);
        else return INSTANCE;
    }

    @Override
    public boolean addPlayer(UUID playerUUID, String playerName)
    {
        return storageManager.addPlayer(playerUUID, playerName, configFields.getStartingPower(), configFields.getGlobalMaxPower());
    }

    @Override
    public float getPlayerPower(UUID playerUUID)
    {
        return storageManager.getPlayerPower(playerUUID);
    }

    @Override
    public boolean setPlayerPower(UUID playerUUID, float power)
    {
        return storageManager.setPlayerPower(playerUUID, power);
    }

    @Override
    public float getPlayerMaxPower(UUID playerUUID)
    {
        return storageManager.getPlayerMaxPower(playerUUID);
    }

    @Override
    public boolean setPlayerMaxPower(UUID playerUUID, float maxpower)
    {
        return storageManager.setPlayerMaxPower(playerUUID, maxpower);
    }

    @Override
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

    @Override
    public IFactionPlayer convertToFactionPlayer(User user) {
        String factionName = "";
        FactionMemberType factionMemberType = null;
        Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if (optionalFaction.isPresent())
        {
            factionName = optionalFaction.get().getName();
            factionMemberType = optionalFaction.get().getPlayerMemberType(user.getUniqueId());
        }

        return new FactionPlayer(user.getName(), user.getUniqueId(), factionName, factionMemberType, this.configFields.getStartingPower(),  this.configFields.getGlobalMaxPower());
    }

    private Optional<String> getLastKnownPlayerName(UUID playerUUID)
    {
        String playerName = storageManager.getPlayerName(playerUUID);
        if(playerName.equals(""))
            return Optional.empty();
        return Optional.of(playerName);
    }

    @Override
    public Optional<Player> getPlayer(UUID playerUUID)
    {
        final Optional<User> oUser = getUser(playerUUID);
        if(!oUser.isPresent())
            return Optional.empty();
        return oUser.get().getPlayer();
    }

    @Override
    public boolean isPlayerOnline(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);
        return oUser.map(User::isOnline).orElse(false);
    }

    @Override
    public Set<String> getServerPlayerNames()
    {
        return storageManager.getServerPlayerNames();
    }

    @Override
    public void setDeathInWarZone(UUID playerUUID, boolean didDieInWarZone)
    {
        storageManager.setDeathInWarzone(playerUUID, didDieInWarZone);
    }

    @Override
    public boolean lastDeathAtWarZone(UUID playerUUID)
    {
       return storageManager.getLastDeathInWarzone(playerUUID);
    }

    @Override
    public boolean checkIfPlayerExists(UUID playerUUID, String playerName)
    {
        return storageManager.checkIfPlayerExists(playerUUID, playerName);
    }

    @Override
    @Nullable
    public FactionMemberType getFactionMemberType(final UUID playerUUID, final Faction faction)
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

    @Override
    public Set<IFactionPlayer> getServerPlayers()
    {
        return storageManager.getServerPlayers();
    }

    @Override
    public void updatePlayerName(final UUID playerUUID, final String playerName)
    {
        this.storageManager.updatePlayerName(playerUUID, playerName);
    }

    private Optional<User> getUser(final UUID playerUUID)
    {
        final Optional<User> oUser = userStorageService.get(playerUUID);
        return oUser;
    }
}
