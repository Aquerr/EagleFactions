package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public class CollideBlockEventListener extends AbstractListener
{
    public CollideBlockEventListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockCollide(final CollideBlockEvent event)
    {
        if(event instanceof CollideBlockEvent.Impact)
            return;

        if(event.source() instanceof FallingBlock)
            return;

        User user = null;
        final Cause cause = event.cause();
        final EventContext context = event.context();
//        if (cause.root() instanceof BlockEntity) {
        user = context.get(EventContextKeys.PLAYER)
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::user)
                .orElse(null);

        if (user == null) {
            if (event instanceof ExplosionEvent) {
                // Check igniter
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living instanceof User) {
                    user = (User) living;
                }
            }
        }

        if(user == null)
            return;

        final BlockType blockType = event.targetBlock().type();
        if(blockType.equals(BlockTypes.AIR.get()))
            return;

        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.uniqueId());
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(event.targetLocation().world().uniqueId(), event.targetLocation().chunkPosition());

        if(optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent())
        {
            if(!optionalChunkFaction.get().getName().equalsIgnoreCase(optionalPlayerFaction.get().getName()))
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onProjectileImpactBlock(final CollideBlockEvent.Impact event)
    {
        if(!(event.source() instanceof Entity))
            return;

        User user = null;
        final Cause cause = event.cause();
        final EventContext context = event.context();
        user = context.get(EventContextKeys.PLAYER)
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .map(ServerPlayer::user)
                .orElse(null);

        if (user == null) {
            if (event instanceof ExplosionEvent) {
                // Check igniter
                final Living living = context.get(EventContextKeys.IGNITER).orElse(null);
                if (living instanceof User) {
                    user = (User) living;
                }
            }
        }

        if(user == null)
            return;

        ServerLocation impactPoint = event.impactPoint();
        Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(impactPoint.world().uniqueId(), impactPoint.chunkPosition());

        if(!optionalChunkFaction.isPresent())
            return;

        Faction chunkFaction = optionalChunkFaction.get();
        Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.uniqueId());
        if(!optionalPlayerFaction.isPresent())
        {
            //Special case for pixelmon... we should consider adding a configurable list in the config file.
            if (StringUtils.containsIgnoreCase(event.cause().root().getClass().getName(), "Pokeball"))
                return;

            event.setCancelled(true);
            return;
        }

        Faction playerFaction = optionalPlayerFaction.get();
        if(playerFaction.getName().equalsIgnoreCase(chunkFaction.getName()))
            return;
        else
        {
            //Special case for pixelmon... we should consider adding a configurable list in the config file.
            if (StringUtils.containsIgnoreCase(event.cause().root().getClass().getName(), "Pokeball"))
                return;

            event.setCancelled(true);
            return;
        }
    }
}
