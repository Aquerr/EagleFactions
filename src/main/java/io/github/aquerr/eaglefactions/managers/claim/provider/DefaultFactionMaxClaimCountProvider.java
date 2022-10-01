package io.github.aquerr.eaglefactions.managers.claim.provider;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.claim.provider.FactionMaxClaimCountProvider;

public class DefaultFactionMaxClaimCountProvider implements FactionMaxClaimCountProvider
{
    private final FactionMaxClaimCountProvider provider;

    public DefaultFactionMaxClaimCountProvider(FactionMaxClaimCountProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public int getMaxClaimCount(Faction faction)
    {
        return provider.getMaxClaimCount(faction);
    }
}
