package io.github.aquerr.eaglefactions.common.scheduling;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import org.spongepowered.api.Sponge;

import java.util.UUID;

public class WorldRegenTask implements EagleFactionsRunnableTask
{
    private final Claim claim;

    public WorldRegenTask(final Claim claim)
    {
        this.claim = claim;
    }

    @Override
    public String getName()
    {
        return UUID.randomUUID().toString();
    }

    @Override
    public void run()
    {
        Sponge.getServer().getWorld(claim.getWorldUUID()).ifPresent(world -> world.regenerateChunk(claim.getChunkPosition()));
    }
}
