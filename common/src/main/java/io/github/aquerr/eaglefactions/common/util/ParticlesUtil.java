package io.github.aquerr.eaglefactions.common.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.scheduling.EagleFactionsConsumerTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class ParticlesUtil
{
	private ParticlesUtil()
	{

	}

	public static void spawnAddAccessParticles(final Claim claim)
	{
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final World world = optionalWorld.get();
		final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.FIREWORKS_SPARK).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 2, 8)).build(), position);
		world.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, position, 5, 10);
	}

	public static void spawnRemoveAccessParticles(final Claim claim)
	{
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(claim.getWorldUUID());
		if(!optionalWorld.isPresent())
			return;

		final World world = optionalWorld.get();
		final Vector3d position = getChunkCenter(world, claim.getChunkPosition());
		world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.LARGE_SMOKE).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).quantity(400).offset(new Vector3d(8, 1, 8)).build(), position);
		world.playSound(SoundTypes.ITEM_FIRECHARGE_USE, position, 5, -10);
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

	public static Vector3d getChunkCenter(final World world, final Vector3i chunkPosition)
	{
		final double x = (chunkPosition.getX() << 4) + 8;
		final double z = (chunkPosition.getZ() << 4) + 8;
		final double y = world.getHighestYAt((int)x, (int)z);
		return new Vector3d(x, y, z);
	}

	public static class HomeParticles implements EagleFactionsConsumerTask<Task>
	{
		private final Player player;
		private final World world;
		private final Location<World> location;

		private final double r = 0.6;
		private final double numberOfParticles = 28;

		private final double angleIncrement = (2 / numberOfParticles) * Math.PI;
		private double angle = 0;

		private final Vector3i lastBlockPosition;

		public HomeParticles(final Player player)
		{
			this.player = player;
			this.world = player.getWorld();
			this.location = player.getLocation();
			this.lastBlockPosition = player.getLocation().getBlockPosition();
		}

		@Override
		public void accept(Task task)
		{
			double x = this.location.getX() + r * Math.cos(angle);
			double z = this.location.getZ() + r * Math.sin(angle);


			world.spawnParticles(ParticleEffect.builder().type(ParticleTypes.END_ROD).quantity(5).offset(Vector3d.from(0, 0.5, 0)).build(), Vector3d.from(x, location.getY() + 0.5, z));

			if (angle + angleIncrement > 360)
			{
				angle = (angle + angleIncrement) - 360;
			}
			else
			{
				angle += angleIncrement;
			}

			if (!this.lastBlockPosition.equals(this.player.getLocation().getBlockPosition()) || !this.player.isOnline())
				task.cancel();
		}
	}
}
