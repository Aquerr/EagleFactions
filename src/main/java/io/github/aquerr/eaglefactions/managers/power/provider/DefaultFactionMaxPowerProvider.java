package io.github.aquerr.eaglefactions.managers.power.provider;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.power.provider.FactionMaxPowerProvider;

public class DefaultFactionMaxPowerProvider implements FactionMaxPowerProvider
{
    private final FactionMaxPowerProvider provider;

    public DefaultFactionMaxPowerProvider(FactionMaxPowerProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public float getFactionMaxPower(Faction faction)
    {
        return provider.getFactionMaxPower(faction);
    }
}
