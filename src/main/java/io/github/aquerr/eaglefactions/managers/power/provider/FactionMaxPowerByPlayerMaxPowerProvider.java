package io.github.aquerr.eaglefactions.managers.power.provider;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.power.provider.FactionMaxPowerProvider;

import java.util.Optional;
import java.util.UUID;

import static io.github.aquerr.eaglefactions.util.MathUtil.round;

public class FactionMaxPowerByPlayerMaxPowerProvider implements FactionMaxPowerProvider
{
    private static final UUID dummyUUID = new UUID(0, 0);

    private final PlayerManager playerManager;

    public FactionMaxPowerByPlayerMaxPowerProvider(PlayerManager playerManager)
    {
        this.playerManager = playerManager;
    }

    @Override
    public float getFactionMaxPower(Faction faction)
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

    private float getPlayerMaxPower(final UUID playerUUID)
    {
        if(playerUUID == null || playerUUID.equals(dummyUUID))
            return 0;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        return optionalFactionPlayer.map(FactionPlayer::getMaxPower).orElse(0F);
    }
}
