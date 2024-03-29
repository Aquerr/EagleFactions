package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.user.UserManager;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlayerManagerImpl implements PlayerManager
{
    private final StorageManager storageManager;
    private final PowerConfig powerConfig;

    private Supplier<UserManager> userManager;

    private final Set<UUID> adminModePlayers = new HashSet<>();

    public PlayerManagerImpl(final StorageManager storageManager, final PowerConfig powerConfig)
    {
        this.storageManager = storageManager;
        this.powerConfig = powerConfig;
        this.userManager = () -> Sponge.server().userManager();
    }

    @Override
    public boolean addPlayer(final UUID playerUUID, final String playerName)
    {
        final FactionPlayer factionPlayer = new FactionPlayerImpl(playerName, playerUUID, null, this.powerConfig.getStartingPower(), this.powerConfig.getGlobalMaxPower(), false);
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
        return Optional.ofNullable(this.storageManager.getPlayer(playerUUID));
    }

    @Override
    public Optional<ServerPlayer> getPlayer(final UUID playerUUID)
    {
        return getUser(playerUUID).flatMap(User::player);
    }

    @Override
    public boolean isPlayerOnline(final UUID playerUUID)
    {
        return getUser(playerUUID).map(User::isOnline).orElse(false);
    }

    @Override
    public Set<FactionPlayer> getServerPlayers()
    {
        return this.storageManager.getServerPlayers();
    }

    private Optional<User> getUser(final UUID playerUUID)
    {
        return userManager.get().load(playerUUID).join();
    }

    @Override
    public boolean hasAdminMode(final User player)
    {
        return player.hasPermission(PluginPermissions.CONSTANT_ADMIN_MODE) || this.adminModePlayers.contains(player.uniqueId());
    }

    @Override
    public boolean activateAdminMode(final User player)
    {
        return this.adminModePlayers.add(player.uniqueId());
    }

    @Override
    public boolean deactivateAdminMode(final User player)
    {
        return this.adminModePlayers.remove(player.uniqueId());
    }

    @Override
    public Set<ServerPlayer> getAdminModePlayers()
    {
        return Sponge.server().onlinePlayers().stream()
                .filter(serverPlayer -> hasAdminMode(serverPlayer.user()))
                .collect(Collectors.toSet());
    }

    @Override
    public void setDeathInWarZone(UUID playerUUID, boolean didDie)
    {
        final FactionPlayer factionPlayer = this.storageManager.getPlayer(playerUUID);
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionPlayer.getFactionName().orElse(null), factionPlayer.getPower(), factionPlayer.getMaxPower(), didDie);
        this.storageManager.savePlayer(updatedPlayer);
    }
}
