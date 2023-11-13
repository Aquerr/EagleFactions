package io.github.aquerr.eaglefactions.util;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import net.kyori.adventure.sound.Sound;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

import static io.github.aquerr.eaglefactions.util.WorldUtil.getChunkTopCenter;

public final class ParticlesUtil
{
	private ParticlesUtil()
	{

	}

	public static void spawnAddAccessParticles(final Claim claim)
	{
		final Optional<ServerWorld> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final ServerWorld world = optionalWorld.get();
		final Vector3d position = getChunkTopCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.FIREWORK).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 2, 8)).build(), position);
		world.playSound(Sound.sound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.AMBIENT, 5, 10), position);
	}

	public static void spawnRemoveAccessParticles(final Claim claim)
	{
		final Optional<ServerWorld> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final ServerWorld world = optionalWorld.get();
		final Vector3d position = getChunkTopCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.LARGE_SMOKE).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 1, 8)).build(), position);
		world.playSound(Sound.sound(SoundTypes.ITEM_FIRECHARGE_USE, Sound.Source.AMBIENT, 5, -10), position);
	}

	public static void spawnClaimParticles(final Claim claim)
	{
		final Optional<ServerWorld> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final ServerWorld world = optionalWorld.get();
		final Vector3d position = getChunkTopCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.END_ROD).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 1, 8)).build(), position);
		world.playSound(Sound.sound(SoundTypes.BLOCK_ENDER_CHEST_OPEN, Sound.Source.AMBIENT, 5, -20), position);
	}

	public static void spawnUnclaimParticles(final Claim claim)
	{
		final Optional<ServerWorld> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final ServerWorld world = optionalWorld.get();
		final Vector3d position = getChunkTopCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.CLOUD).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(800).offset(new Vector3d(8, 1, 8)).build(), position);
		world.playSound(Sound.sound(SoundTypes.ENTITY_SHULKER_SHOOT, Sound.Source.AMBIENT, 5, -20), position);
	}

	public static void spawnDestroyClaimParticles(final Claim claim)
	{
		final Optional<ServerWorld> optionalWorld = WorldUtil.getWorldByUUID(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final ServerWorld world = optionalWorld.get();
		final Vector3d position = getChunkTopCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.FLAME).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(800).offset(new Vector3d(8, 1, 8)).build(), position);
		world.playSound(Sound.sound(SoundTypes.ENTITY_BLAZE_SHOOT, Sound.Source.AMBIENT, 5, -20), position);
	}
}
