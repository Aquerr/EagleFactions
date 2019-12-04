package io.github.aquerr.eaglefactions.common.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class ParticlesUtil
{
	private ParticlesUtil()
	{

	}

	public static void spawnClaimParticles(final Claim claim)
	{
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final World world = optionalWorld.get();
		final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.END_ROD).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 1, 8)).build(), position);
		world.playSound(SoundTypes.BLOCK_ENDERCHEST_OPEN, position, 5, -20);
	}

	public static void spawnUnclaimParticles(final Claim claim)
	{
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final World world = optionalWorld.get();
		final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.CLOUD).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(800).offset(new Vector3d(8, 1, 8)).build(), position);
		world.playSound(SoundTypes.ENTITY_SHULKER_SHOOT, position, 5, -20);
	}

	public static void spawnDestroyClaimParticles(final Claim claim)
	{
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final World world = optionalWorld.get();
		final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.FLAME).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(800).offset(new Vector3d(8, 1, 8)).build(), position);
		world.playSound(SoundTypes.ENTITY_BLAZE_SHOOT, position, 5, -20);
	}

	private static Vector3d getChunkCenter(final World world, final Vector3i chunkPosition)
	{
		final double x = (chunkPosition.getX() << 4) + 8;
		final double z = (chunkPosition.getZ() << 4) + 8;
		final double y = world.getHighestYAt((int)x, (int)z);
		return new Vector3d(x, y, z);
	}
}
