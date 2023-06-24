package io.github.aquerr.eaglefactions.util;

import com.mojang.math.Vector3d;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.math.Vector3i;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class ParticlesUtil
{
    private ParticlesUtil()
    {

    }

    public static void spawnAddAccessParticles(final Claim claim)
    {
        final Optional<Level> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
        if(!optionalWorld.isPresent())
            return;

//        final Level world = optionalWorld.get();
//        final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
//        world.addParticle(ParticleEffect.builder().type(ParticleTypes.FIREWORK).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 2, 8)).build(), position);
//        world.playSound(Sound.sound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.AMBIENT, 5, 10), position);
    }

    public static void spawnRemoveAccessParticles(final Claim claim)
    {
        final Optional<Level> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
        if(!optionalWorld.isPresent())
            return;

//        final ServerWorld world = optionalWorld.get();
//        final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
//        world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.LARGE_SMOKE).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 1, 8)).build(), position);
//        world.playSound(Sound.sound(SoundTypes.ITEM_FIRECHARGE_USE, Sound.Source.AMBIENT, 5, -10), position);
    }

    public static void spawnClaimParticles(final Claim claim)
    {
        final Optional<Level> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
        if(!optionalWorld.isPresent())
            return;

//        final ServerWorld world = optionalWorld.get();
//        final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
//        world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.END_ROD).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 1, 8)).build(), position);
//        world.playSound(Sound.sound(SoundTypes.BLOCK_ENDER_CHEST_OPEN, Sound.Source.AMBIENT, 5, -20), position);
    }

    public static void spawnUnclaimParticles(final Claim claim)
    {
        final Optional<Level> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
        if(!optionalWorld.isPresent())
            return;

//        final ServerWorld world = optionalWorld.get();
//        final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
//        world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.CLOUD).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(800).offset(new Vector3d(8, 1, 8)).build(), position);
//        world.playSound(Sound.sound(SoundTypes.ENTITY_SHULKER_SHOOT, Sound.Source.AMBIENT, 5, -20), position);
    }

    public static void spawnDestroyClaimParticles(final Claim claim)
    {
        final Optional<Level> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
        if(!optionalWorld.isPresent())
            return;

//        final ServerWorld world = optionalWorld.get();
//        final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
//        world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.FLAME).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(800).offset(new Vector3d(8, 1, 8)).build(), position);
//        world.playSound(Sound.sound(SoundTypes.ENTITY_BLAZE_SHOOT, Sound.Source.AMBIENT, 5, -20), position);
    }

    public static Vector3d getChunkCenter(final Level world, final Vector3i chunkPosition)
    {
        final double x = (chunkPosition.getX() << 4) + 8;
        final double z = (chunkPosition.getZ() << 4) + 8;
//        final double y = world.highestYAt((int)x, (int)z);
        return new Vector3d(x, 0, z);
    }

//    public static class HomeParticles implements EagleFactionsConsumerTask<ScheduledTask>
//    {
//        private final ServerPlayer player;
//        private final Level world;
//        private final ServerLocation location;
//
//        private final double r = 0.6;
//        private final double numberOfParticles = 28;
//
//        private final double angleIncrement = (2 / numberOfParticles) * Math.PI;
//        private double angle = 0;
//
//        private final BlockPos lastBlockPosition;
//
//        public HomeParticles(final ServerPlayer player)
//        {
//            this.player = player;
//            this.world = player.getLevel();
//            this.location = ServerLocation.of(this.world, player.blockPosition());
//            this.lastBlockPosition = this.location.getBlockPos();
//        }
//
////        @Override
////        public void accept(ScheduledTask task)
////        {
////            double x = this.location.x() + r * Math.cos(angle);
////            double z = this.location.z() + r * Math.sin(angle);
////
////
////            world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.END_ROD).quantity(5).offset(Vector3d.from(0, 0.5, 0)).build(), Vector3d.from(x, location.y() + 0.5, z));
////
////            if (angle + angleIncrement > 360)
////            {
////                angle = (angle + angleIncrement) - 360;
////            }
////            else
////            {
////                angle += angleIncrement;
////            }
////
////            if (!this.lastBlockPosition.equals(this.player.serverLocation().blockPosition()) || !this.player.isOnline())
////                task.cancel();
////        }
//    }
}
