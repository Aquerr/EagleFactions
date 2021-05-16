package io.github.aquerr.eaglefactions.common.managers;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionType;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionResult;
import io.github.aquerr.eaglefactions.common.config.ProtectionConfigImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProtectionManagerTest
{
	@Mock
	private ChatConfig chatConfig;
	@Mock
	private ProtectionConfig protectionConfig;
	@Mock
	private FactionLogic factionLogic;
	@Mock
	private PlayerManager playerManager;
	@Mock
	private PermsManager permsManager;

//	@BeforeEach
//	void prepareMocks()
//	{
//		chatConfig = mock(ChatConfig.class);
//		protectionConfig = mock(ProtectionConfig.class);
//		factionLogic = mock(FactionLogic.class);
//		playerManager = mock(PlayerManager.class);
//		permsManager = mock(PermsManager.class);
//	}

	@InjectMocks
	private ProtectionManagerImpl protectionManager;

	@Test
	void bucketShouldBeWhitelistedInSafeZone()
	{
		//given
		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
		when(whiteList.isItemWhiteListed("minecraft:bucket")).thenReturn(true);
		when(protectionConfig.getSafeZoneWhitelists()).thenReturn(whiteList);

		//when
		final boolean result = protectionManager.isItemWhitelisted("minecraft:bucket", FactionType.SAFE_ZONE);

		//then
		verify(protectionConfig).getSafeZoneWhitelists();

		assertTrue(result);
	}

	@Test
	void bucketShouldNotBeWhitelistedInSafeZone()
	{
		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
		when(protectionConfig.getSafeZoneWhitelists()).thenReturn(whiteList);

		//then

		final boolean result = protectionManager.isItemWhitelisted("minecraft:bucket", FactionType.SAFE_ZONE);

		verify(protectionConfig).getSafeZoneWhitelists();
		assertFalse(result);
	}

	@Test
	void stoneShouldBeWhiteListedForPlaceAndDestroyInWarZone()
	{
		//given
		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
		when(whiteList.isBlockWhitelistedForPlaceDestroy("minecraft:stone")).thenReturn(true);
		when(protectionConfig.getWarZoneWhitelists()).thenReturn(whiteList);

		//when
		final boolean result = protectionManager.isBlockWhitelistedForPlaceDestroy("minecraft:stone", FactionType.WAR_ZONE);

		//then
		verify(protectionConfig).getWarZoneWhitelists();
		assertTrue(result);
	}

	@Test
	void stoneShouldNotBeWhiteListedForPlaceAndDestroyInWarZone()
	{
		//given
		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
		when(protectionConfig.getWarZoneWhitelists()).thenReturn(whiteList);

		//when
		final boolean result = protectionManager.isBlockWhitelistedForPlaceDestroy("minecraft:stone", FactionType.WAR_ZONE);

		//then
		verify(protectionConfig).getWarZoneWhitelists();
		assertFalse(result);
	}

	@Test
	void adminShouldBeAllowedToAttackEntity()
	{
		//given
		final Player player = mock(Player.class);
		final Entity entity = mock(Entity.class);

		//when
		when(playerManager.hasAdminMode(player)).thenReturn(true);

		//then
		final boolean result = protectionManager.canHitEntity(entity, player, false).hasAccess();
		assertTrue(result);
	}

	@Test
	void placingBlocksInWildernessIsForbiddenWhenProtectWildernessIsOn()
	{
		//given
		final Player player = mock(Player.class);
		final World world = mock(World.class);
		final Location<World> location = new Location<>(world, 0, 0, 0);
		final BlockSnapshot blockSnapshot = mock(BlockSnapshot.class);
		final BlockState blockState = mock(BlockState.class);
		final ProtectionConfig.WhiteList wildernessWhitelist = mock(ProtectionConfig.WhiteList.class);
		location.setBlock(Mockito.mock(BlockState.class));

		try
		{
			final Field field = location.getClass().getDeclaredField("chunkPosition");
			if (!field.isAccessible())
				field.setAccessible(true);
			field.set(location, Vector3i.from(0, 0, 0));
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		//when
		when(world.getUniqueId()).thenReturn(UUID.randomUUID());
		when(protectionConfig.shouldProtectWildernessFromPlayers()).thenReturn(true);
		when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
		when(world.getBlock(any(Vector3i.class))).thenReturn(blockState);
		when(blockState.getId()).thenReturn("id");
		when(protectionConfig.getWildernessWhitelists()).thenReturn(wildernessWhitelist);

		//then
		final ProtectionResult result = protectionManager.canPlace(blockSnapshot, player, false);
		assertFalse(result.hasAccess());
	}

	@Test
	void placingBlocksInSafeZoneShouldBeBlockedWhenPlayerHasNotPermissions()
	{
		//given
		final Player player = mock(Player.class);
		final World world = mock(World.class);
		final Location<World> location = new Location<>(world, 0, 0, 0);
		final Faction safezoneFaction = FactionImpl.builder("SafeZone", Text.of("SZ"), UUID.randomUUID()).build();
		final ItemStack itemStack = mock(ItemStack.class);
		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
		final BlockSnapshot blockSnapshot = mock(BlockSnapshot.class);

		try
		{
			final Field field = location.getClass().getDeclaredField("chunkPosition");
			if (!field.isAccessible())
				field.setAccessible(true);
			field.set(location, Vector3i.from(0, 0, 0));
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		//when
		when(protectionConfig.getSafeZoneWhitelists()).thenReturn(whiteList);
		when(world.getUniqueId()).thenReturn(UUID.randomUUID());
		when(factionLogic.getFactionByChunk(world.getUniqueId(), location.getChunkPosition())).thenReturn(Optional.of(safezoneFaction));
		when(itemStack.getType()).thenReturn(mock(ItemType.class));
		when(itemStack.getType().getId()).thenReturn("minecraft:stone");
		when(player.getItemInHand(HandTypes.MAIN_HAND)).thenReturn(Optional.of(itemStack));
		when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));

		//then
		final ProtectionResult result = protectionManager.canPlace(blockSnapshot, player, false);
		assertFalse(result.hasAccess());
	}
}
