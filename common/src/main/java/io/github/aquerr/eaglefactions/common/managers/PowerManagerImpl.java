package io.github.aquerr.eaglefactions.common.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.common.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.common.scheduling.PowerIncrementTask;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class PowerManagerImpl implements PowerManager
{
    private final PlayerManager playerManager;
    private final PowerConfig powerConfig;

    private final UUID dummyUUID = new UUID(0, 0);

    public PowerManagerImpl(final PlayerManager playerManager, final PowerConfig powerConfig)
    {
        this.playerManager = playerManager;
        this.powerConfig = powerConfig;
    }

    @Override
    public int getFactionMaxClaims(final Faction faction)
    {
        float power = getFactionPower(faction);
        return (int)power;
    }

    @Override
    public float getPlayerPower(@Nullable final UUID playerUUID)
    {
        if (playerUUID == null || playerUUID.equals(dummyUUID))
            return 0;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        return optionalFactionPlayer.map(FactionPlayer::getPower).orElse(0F);
    }

    @Override
    public float getFactionPower(final Faction faction)
    {
        if(faction.isSafeZone() || faction.isWarZone())
            return 9999.0f;

        float factionPower = 0;
        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            factionPower = factionPower + getPlayerPower(faction.getLeader());
        }
        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer: faction.getOfficers())
            {
                float officerPower = getPlayerPower(officer);
                factionPower =factionPower + officerPower;
            }
        }
        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member: faction.getMembers())
            {
                float memberPower = getPlayerPower(member);
                factionPower = factionPower + memberPower;
            }
        }
        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                float recruitPower = getPlayerPower(recruit);
                factionPower = factionPower + recruitPower;
            }
        }

        return round(factionPower, 2);
    }

    @Override
    public float getFactionMaxPower(final Faction faction)
    {
        if(faction.isSafeZone() || faction.isWarZone())
            return 9999.0f;

        float factionMaxPower = 0;

        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            factionMaxPower = factionMaxPower + getPlayerMaxPower(faction.getLeader());
        }

        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer : faction.getOfficers())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(officer);
            }
        }

        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member : faction.getMembers())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(member);
            }
        }

        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(recruit);
            }
        }

        return round(factionMaxPower, 2);
    }

    @Override
    public float getPlayerMaxPower(final UUID playerUUID)
    {
        if(playerUUID == null || playerUUID.equals(dummyUUID))
            return 0;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        return optionalFactionPlayer.map(FactionPlayer::getMaxPower).orElse(0F);
    }

    @Override
    public boolean setPlayerPower(UUID playerUUID, float power)
    {
        if (playerUUID == null || playerUUID.equals(dummyUUID))
            return false;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        if (!optionalFactionPlayer.isPresent())
            return false;
        final FactionPlayer factionPlayer = optionalFactionPlayer.get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionPlayer.getFactionName().orElse(null), round(power, 2), factionPlayer.getMaxPower(), factionPlayer.getFactionRole(), factionPlayer.diedInWarZone());
        return this.playerManager.savePlayer(updatedPlayer);
    }

    @Override
    public boolean setPlayerMaxPower(UUID playerUUID, float maxpower)
    {
        if (playerUUID == null || playerUUID.equals(dummyUUID))
            return false;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        if (!optionalFactionPlayer.isPresent())
            return false;
        final FactionPlayer factionPlayer = optionalFactionPlayer.get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionPlayer.getFactionName().orElse(null), factionPlayer.getPower(), maxpower, factionPlayer.getFactionRole(), factionPlayer.diedInWarZone());
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

    public static float round(final float number, final int decimalPlace) {
        int pow = 10;
        for (int i = 1; i < decimalPlace; i++)
            pow *= 10;
        float tmp = number * pow;
        return ( (float) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
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
