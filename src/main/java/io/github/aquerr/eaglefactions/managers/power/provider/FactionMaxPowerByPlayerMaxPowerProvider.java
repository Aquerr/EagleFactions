package io.github.aquerr.eaglefactions.managers.power.provider;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
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

        for (final FactionMember factionMember : faction.getMembers())
        {
            factionMaxPower = factionMaxPower + getPlayerMaxPower(factionMember.getUniqueId());
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
