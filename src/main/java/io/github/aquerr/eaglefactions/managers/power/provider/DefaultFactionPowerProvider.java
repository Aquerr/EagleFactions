package io.github.aquerr.eaglefactions.managers.power.provider;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.power.provider.FactionPowerProvider;

public class DefaultFactionPowerProvider implements FactionPowerProvider
{
    private final FactionPowerProvider provider;

    public DefaultFactionPowerProvider(FactionPowerProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public float getFactionPower(Faction faction)
    {
        return this.provider.getFactionPower(faction);
    }
}
