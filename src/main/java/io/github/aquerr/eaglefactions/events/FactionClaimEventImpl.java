package io.github.aquerr.eaglefactions.events;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.events.FactionClaimEvent;
import io.github.aquerr.eaglefactions.api.math.Vector3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FactionClaimEventImpl extends FactionUnclaimEventImpl implements FactionClaimEvent.Claim
{
    FactionClaimEventImpl(final ServerPlayer creator, final Faction faction, final Level world, final Vector3i chunkPosition)
    {
        super(creator, faction, world, chunkPosition);
    }

    @Override
    public boolean isClaimedByItems()
    {
        return EagleFactionsPlugin.getPlugin().getConfiguration().getFactionsConfig().shouldClaimByItems();
    }

    static class Pre extends FactionClaimEventImpl implements FactionClaimEvent.Claim.Pre
    {
        Pre(ServerPlayer creator, Faction faction, Level world, Vector3i chunkPosition)
        {
            super(creator, faction, world, chunkPosition);
        }
    }

    static class Post extends FactionClaimEventImpl implements FactionClaimEvent.Claim.Post
    {
        Post(ServerPlayer creator, Faction faction, Level world, Vector3i chunkPosition)
        {
            super(creator, faction, world, chunkPosition);
        }
    }
}