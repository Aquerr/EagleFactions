package io.github.aquerr.eaglefactions.common.managers;

import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.PluginPermissions;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Identifiable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerManagerImpl implements PlayerManager
{
    private final StorageManager storageManager;
    private final FactionLogic factionLogic;
    private final FactionsConfig factionsConfig;
    private final PowerConfig powerConfig;

    private UserStorageService userStorageService;

    private final Set<UUID> adminModePlayers = new HashSet<>();

    public PlayerManagerImpl(final StorageManager storageManager, final FactionLogic factionLogic, final FactionsConfig factionsConfig, final PowerConfig powerConfig)
    {
        this.storageManager = storageManager;
        this.factionLogic = factionLogic;
        this.factionsConfig = factionsConfig;
        this.powerConfig = powerConfig;

        Optional<UserStorageService> optionalUserStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
        optionalUserStorageService.ifPresent(x -> userStorageService = x);
    }

    @Override
    public boolean addPlayer(final UUID playerUUID, final String playerName)
    {
        final FactionPlayer factionPlayer = new FactionPlayerImpl(playerName, playerUUID, null, this.powerConfig.getStartingPower(), this.powerConfig.getGlobalMaxPower(), null, false);
        return storageManager.savePlayer(factionPlayer);
    }

    @Override
    public boolean savePlayer(FactionPlayer factionPlayer)
    {
        return this.storageManager.savePlayer(factionPlayer);
    }

    @Override
    public Optional<FactionPlayer> getFactionPlayer(final UUID playerUUID)
    {
        final FactionPlayer factionPlayer = this.storageManager.getPlayer(playerUUID);
        if (factionPlayer == null)
            return Optional.empty();
        else return Optional.of(factionPlayer);
    }

    @Override
    public Optional<Player> getPlayer(final UUID playerUUID)
    {
        final Optional<User> oUser = getUser(playerUUID);
        return oUser.flatMap(User::getPlayer);
    }

    @Override
    public boolean isPlayerOnline(final UUID playerUUID)
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
    public Set<FactionPlayer> getServerPlayers()
    {
        return this.storageManager.getServerPlayers();
    }

    private Optional<User> getUser(final UUID playerUUID)
    {
        return userStorageService.get(playerUUID);
    }

    @Override
    public boolean hasAdminMode(final User player)
    {
        return player.hasPermission(PluginPermissions.ADMIN_MODE) || this.adminModePlayers.contains(player.getUniqueId());
    }

    @Override
    public boolean activateAdminMode(final User player)
    {
        return this.adminModePlayers.add(player.getUniqueId());
    }

    @Override
    public boolean deactivateAdminMode(final User player)
    {
        return this.adminModePlayers.remove(player.getUniqueId());
    }

    @Override
    public Set<UUID> getAdminModePlayers()
    {
        return Sponge.getServer().getOnlinePlayers().stream().filter(this::hasAdminMode).map(Identifiable::getUniqueId).collect(Collectors.toSet());
    }

    @Override
    public void setDeathInWarZone(UUID playerUUID, boolean didDie)
    {
        final FactionPlayer factionPlayer = this.storageManager.getPlayer(playerUUID);
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionPlayer.getFactionName().orElse(null), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.getFactionRole(), didDie);
        this.storageManager.savePlayer(updatedPlayer);
    }
}
