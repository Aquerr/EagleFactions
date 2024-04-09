package io.github.aquerr.eaglefactions.managers.claim;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

class ClaimContextImpl implements ClaimContext
{
    private final ServerPlayer serverPlayer;
    private final Faction faction;
    private final ServerLocation serverLocation;

    public ClaimContextImpl(ServerPlayer serverPlayer,
                            Faction faction,
                            ServerLocation serverLocation)
    {
        this.serverPlayer = serverPlayer;
        this.faction = faction;
        this.serverLocation = serverLocation;
    }

    @Override
    public ServerPlayer getServerPlayer()
    {
        return this.serverPlayer;
    }

    @Override
    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public ServerLocation getServerLocation()
    {
        return this.serverLocation;
    }
}
