package io.github.aquerr.eaglefactions.managers.power.provider;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.power.provider.FactionPowerProvider;

import java.util.Optional;
import java.util.UUID;

import static io.github.aquerr.eaglefactions.util.MathUtil.round;

public class FactionPowerByPlayerPowerProvider implements FactionPowerProvider
{
    private static final UUID dummyUUID = new UUID(0, 0);

    private final PlayerManager playerManager;

    public FactionPowerByPlayerPowerProvider(PlayerManager playerManager)
    {
        this.playerManager = playerManager;
    }

    @Override
    public float getFactionPower(Faction faction)
    {
        if(faction.isSafeZone() || faction.isWarZone())
            return 9999.0f;

        float factionPower = 0;

        for (final FactionMember factionMember : faction.getMembers())
        {
            factionPower = factionPower + getPlayerPower(factionMember.getUniqueId());
        }

        return round(factionPower, 2);
    }

    private float getPlayerPower(UUID playerUUID)
    {
        if (playerUUID == null || playerUUID.equals(dummyUUID))
            return 0;

        final Optional<FactionPlayer> optionalFactionPlayer = this.playerManager.getFactionPlayer(playerUUID);
        return optionalFactionPlayer.map(FactionPlayer::getPower).orElse(0F);
    }
}
