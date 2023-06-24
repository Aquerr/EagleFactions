package io.github.aquerr.eaglefactions.managers.claim.provider;


import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.managers.claim.provider.FactionMaxClaimCountProvider;

public class FactionMaxClaimCountByPlayerPowerProvider implements FactionMaxClaimCountProvider
{
    private final PowerManager powerManager;

    public FactionMaxClaimCountByPlayerPowerProvider(PowerManager powerManager)
    {
        this.powerManager = powerManager;
    }

    @Override
    public int getMaxClaimCount(Faction faction)
    {
        float power = powerManager.getFactionPower(faction);
        return (int)power;
    }
}