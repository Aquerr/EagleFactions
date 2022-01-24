package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.util.WorldUtil;

public class WorldRegenTask implements EagleFactionsRunnableTask
{
    private final Claim claim;

    public WorldRegenTask(final Claim claim)
    {
        this.claim = claim;
    }

    @Override
    public void run()
    {
        WorldUtil.getWorldByUUID(claim.getWorldUUID())
                .ifPresent(serverWorld -> serverWorld.chunkManager().regenerateChunk(claim.getChunkPosition()));
    }
}
