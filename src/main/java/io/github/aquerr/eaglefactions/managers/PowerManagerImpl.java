package io.github.aquerr.eaglefactions.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.managers.power.provider.FactionMaxPowerProvider;
import io.github.aquerr.eaglefactions.api.managers.power.provider.FactionPowerProvider;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.logic.FactionLogicImpl;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.scheduling.PowerIncrementTask;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.github.aquerr.eaglefactions.util.MathUtil.round;

@Singleton
public class PowerManagerImpl implements PowerManager
{
    private final Set<FactionMaxPowerProvider> factionMaxPowerProviders = new HashSet<>();
    private final Set<FactionPowerProvider> factionPowerProviders = new HashSet<>();

    private final PlayerManager playerManager;
    private final PowerConfig powerConfig;

    public PowerManagerImpl(final PlayerManager playerManager, final PowerConfig powerConfig)
    {
        this.playerManager = playerManager;
        this.powerConfig = powerConfig;
    }

    @Override
    public void addFactionPowerProvider(FactionPowerProvider provider)
    {
        this.factionPowerProviders.add(provider);
    }

    @Override
    public void addFactionMaxPowerProvider(FactionMaxPowerProvider provider)
    {
        this.factionMaxPowerProviders.add(provider);
    }

    @Override
    public void setFactionPowerProviders(Set<FactionPowerProvider> providers)
    {
        this.factionPowerProviders.clear();
        this.factionPowerProviders.addAll(providers);
    }

    @Override
    public void setFactionMaxPowerProviders(Set<FactionMaxPowerProvider> providers)
    {
        this.factionMaxPowerProviders.clear();
        this.factionMaxPowerProviders.addAll(providers);
    }

    @Override
    public Set<FactionPowerProvider> getFactionPowerProviders()
    {
        return Collections.unmodifiableSet(this.factionPowerProviders);
    }

    @Override
    public Set<FactionMaxPowerProvider> getFactionMaxPowerProviders()
    {
        return Collections.unmodifiableSet(this.factionMaxPowerProviders);
    }

    @Override
    public float getPlayerPower(@Nullable final UUID playerUUID)
    {
        if (playerUUID == null)
            return 0;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        return optionalFactionPlayer.map(FactionPlayer::getPower).orElse(0F);
    }

    @Override
    public float getFactionPower(final Faction faction)
    {
        float power = 0;
        for (final FactionPowerProvider factionPowerProvider : this.factionPowerProviders)
        {
            power = power + factionPowerProvider.getFactionPower(faction);
        }
        return power;
    }

    @Override
    public float getFactionMaxPower(final Faction faction)
    {
        float maxpower = 0;
        for (final FactionMaxPowerProvider factionMaxPowerProvider : this.factionMaxPowerProviders)
        {
            maxpower = maxpower + factionMaxPowerProvider.getFactionMaxPower(faction);
        }
        return maxpower;
    }

    @Override
    public float getPlayerMaxPower(final UUID playerUUID)
    {
        if(playerUUID == null)
            return 0;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        return optionalFactionPlayer.map(FactionPlayer::getMaxPower).orElse(0F);
    }

    @Override
    public boolean setPlayerPower(UUID playerUUID, float power)
    {
        if (playerUUID == null)
            return false;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        if (!optionalFactionPlayer.isPresent())
            return false;
        final FactionPlayer factionPlayer = optionalFactionPlayer.get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionPlayer.getFactionName().orElse(null), round(power, 2), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        return this.playerManager.savePlayer(updatedPlayer);
    }

    @Override
    public boolean setPlayerMaxPower(UUID playerUUID, float maxpower)
    {
        if (playerUUID == null)
            return false;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        if (!optionalFactionPlayer.isPresent())
            return false;
        final FactionPlayer factionPlayer = optionalFactionPlayer.get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionPlayer.getFactionName().orElse(null), factionPlayer.getPower(), maxpower, factionPlayer.diedInWarZone());
        return this.playerManager.savePlayer(updatedPlayer);
    }

    @Override
    public void addPower(final UUID playerUUID, final boolean isKillAward)
    {
        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        if (!optionalFactionPlayer.isPresent())
            return;

        final float playerPower = optionalFactionPlayer.get().getPower();
        final float playerMaxPower = optionalFactionPlayer.get().getMaxPower();

        if (isKillAward)
        {
            setPlayerPower(playerUUID, round(Math.min(playerPower + this.powerConfig.getKillAward(), playerMaxPower), 2));
        }
        else
        {
            setPlayerPower(playerUUID, round(Math.min(playerPower + this.powerConfig.getPowerIncrement(), playerMaxPower), 2));
        }
    }

    @Override
    public void startIncreasingPower(final UUID playerUUID)
    {
        final EagleFactionsScheduler eagleFactionsScheduler = EagleFactionsScheduler.getInstance();
        eagleFactionsScheduler.scheduleWithDelayedInterval(new PowerIncrementTask(this.playerManager, this, this.powerConfig, playerUUID), 0, TimeUnit.SECONDS, 1, TimeUnit.MINUTES);
    }

    @Override
    public void decreasePower(final UUID playerUUID)
    {
        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        if (!optionalFactionPlayer.isPresent())
            return;

        float playerPower = optionalFactionPlayer.get().getPower();
        if(playerPower - powerConfig.getPowerDecrement() > 0)
        {
            setPlayerPower(playerUUID, playerPower - powerConfig.getPowerDecrement());
        }
        else
        {
            setPlayerPower(playerUUID, 0);
        }
    }

    @Override
    public void penalty(final UUID playerUUID)
    {
        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        if (!optionalFactionPlayer.isPresent())
            return;

        float playerPower = optionalFactionPlayer.get().getPower();
        float penalty = powerConfig.getPenalty();

        if(playerPower - penalty > 0)
        {
            setPlayerPower(playerUUID, playerPower - penalty);
        }
        else
        {
            setPlayerPower(playerUUID, 0);
        }
    }
}
