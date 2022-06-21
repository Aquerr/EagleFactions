//package io.github.aquerr.eaglefactions.managers;
//
//import io.github.aquerr.eaglefactions.api.config.ChatConfig;
//import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
//import io.github.aquerr.eaglefactions.api.entities.Faction;
//import io.github.aquerr.eaglefactions.api.entities.FactionType;
//import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
//import io.github.aquerr.eaglefactions.api.managers.PermsManager;
//import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
//import io.github.aquerr.eaglefactions.api.managers.ProtectionResult;
//import io.github.aquerr.eaglefactions.entities.FactionImpl;
//import net.kyori.adventure.text.Component;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.spongepowered.api.block.BlockSnapshot;
//import org.spongepowered.api.block.BlockState;
//import org.spongepowered.api.data.type.HandTypes;
//import org.spongepowered.api.entity.Entity;
//import org.spongepowered.api.entity.living.player.Player;
//import org.spongepowered.api.entity.living.player.User;
//import org.spongepowered.api.entity.living.player.server.ServerPlayer;
//import org.spongepowered.api.item.ItemType;
//import org.spongepowered.api.item.inventory.ItemStack;
//import org.spongepowered.api.world.Location;
//import org.spongepowered.api.world.World;
//import org.spongepowered.api.world.server.ServerLocation;
//import org.spongepowered.api.world.server.ServerWorld;
//import org.spongepowered.math.vector.Vector3i;
//
//import java.lang.reflect.Field;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class ProtectionManagerTest
//{
//	@Mock
//	private ChatConfig chatConfig;
//	@Mock
//	private ProtectionConfig protectionConfig;
//	@Mock
//	private FactionLogic factionLogic;
//	@Mock
//	private PlayerManager playerManager;
//	@Mock
//	private PermsManager permsManager;
//
////	@BeforeEach
////	void prepareMocks()
////	{
////		chatConfig = mock(ChatConfig.class);
////		protectionConfig = mock(ProtectionConfig.class);
////		factionLogic = mock(FactionLogic.class);
////		playerManager = mock(PlayerManager.class);
////		permsManager = mock(PermsManager.class);
////	}
//
//	@InjectMocks
//	private ProtectionManagerImpl protectionManager;
//
//	@Test
//	void bucketShouldBeWhitelistedInSafeZone()
//	{
//		//given
//		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
//		when(whiteList.isItemWhiteListed("minecraft:bucket")).thenReturn(true);
//		when(protectionConfig.getSafeZoneWhitelists()).thenReturn(whiteList);
//
//		//when
//		final boolean result = protectionManager.isItemWhitelisted("minecraft:bucket", FactionType.SAFE_ZONE);
//
//		//then
//		verify(protectionConfig).getSafeZoneWhitelists();
//
//		assertTrue(result);
//	}
//
//	@Test
//	void bucketShouldNotBeWhitelistedInSafeZone()
//	{
//		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
//		when(protectionConfig.getSafeZoneWhitelists()).thenReturn(whiteList);
//
//		//then
//
//		final boolean result = protectionManager.isItemWhitelisted("minecraft:bucket", FactionType.SAFE_ZONE);
//
//		verify(protectionConfig).getSafeZoneWhitelists();
//		assertFalse(result);
//	}
//
//	@Test
//	void stoneShouldBeWhiteListedForPlaceAndDestroyInWarZone()
//	{
//		//given
//		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
//		when(whiteList.isBlockWhitelistedForPlaceDestroy("minecraft:stone")).thenReturn(true);
//		when(protectionConfig.getWarZoneWhitelists()).thenReturn(whiteList);
//
//		//when
//		final boolean result = protectionManager.isBlockWhitelistedForPlaceDestroy("minecraft:stone", FactionType.WAR_ZONE);
//
//		//then
//		verify(protectionConfig).getWarZoneWhitelists();
//		assertTrue(result);
//	}
//
//	@Test
//	void stoneShouldNotBeWhiteListedForPlaceAndDestroyInWarZone()
//	{
//		//given
//		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
//		when(protectionConfig.getWarZoneWhitelists()).thenReturn(whiteList);
//
//		//when
//		final boolean result = protectionManager.isBlockWhitelistedForPlaceDestroy("minecraft:stone", FactionType.WAR_ZONE);
//
//		//then
//		verify(protectionConfig).getWarZoneWhitelists();
//		assertFalse(result);
//	}
//
//	@Test
//	void adminShouldBeAllowedToAttackEntity()
//	{
//		//given
//		final ServerPlayer player = mock(ServerPlayer.class);
//		final Entity entity = mock(Entity.class);
//		final User user = mock(User.class);
//		when(player.user()).thenReturn(user);
//		when(playerManager.hasAdminMode(player.user())).thenReturn(true);
//
//		//when
//		final boolean result = protectionManager.canHitEntity(entity, player, false).hasAccess();
//
//		//then
//		assertTrue(result);
//	}
//
//	@Test
//	void placingBlocksInWildernessIsForbiddenWhenProtectWildernessIsOn()
//	{
//		//given
//		final ServerPlayer player = mock(ServerPlayer.class);
//		final ServerWorld world = mock(ServerWorld.class);
//		final ServerLocation location = ServerLocation.of(world, 0, 0, 0);
//		final BlockSnapshot blockSnapshot = mock(BlockSnapshot.class);
//		final BlockState blockState = mock(BlockState.class);
//		final ProtectionConfig.WhiteList wildernessWhitelist = mock(ProtectionConfig.WhiteList.class);
//		location.setBlock(Mockito.mock(BlockState.class));
//
//		try
//		{
//			final Field field = location.getClass().getDeclaredField("chunkPosition");
//			if (!field.isAccessible())
//				field.setAccessible(true);
//			field.set(location, Vector3i.from(0, 0, 0));
//		}
//		catch (NoSuchFieldException | IllegalAccessException e)
//		{
//			e.printStackTrace();
//		}
//
//		when(world.uniqueId()).thenReturn(UUID.randomUUID());
//		when(protectionConfig.shouldProtectWildernessFromPlayers()).thenReturn(true);
//		when(blockSnapshot.location()).thenReturn(Optional.of(location));
//		when(world.block(any(Vector3i.class))).thenReturn(blockState);
//		when(blockState.toString()).thenReturn("id");
//		when(protectionConfig.getWildernessWhitelists()).thenReturn(wildernessWhitelist);
//
//		//when
//		final ProtectionResult result = protectionManager.canPlace(blockSnapshot, player.user(), false);
//
//		//then
//		assertFalse(result.hasAccess());
//	}
//
//	@Test
//	void placingBlocksInSafeZoneShouldBeBlockedWhenPlayerHasNotPermissions()
//	{
//		//given
//		final ServerPlayer player = mock(ServerPlayer.class);
//		final ServerWorld world = mock(ServerWorld.class);
//		final ServerLocation location = ServerLocation.of(world, 0, 0, 0);
//		final Faction safezoneFaction = FactionImpl.builder("SafeZone", Component.text("SZ"), UUID.randomUUID()).build();
//		final ItemStack itemStack = mock(ItemStack.class);
//		ProtectionConfig.WhiteList whiteList = Mockito.mock(ProtectionConfig.WhiteList.class);
//		final BlockSnapshot blockSnapshot = mock(BlockSnapshot.class);
//
//		try
//		{
//			final Field field = location.getClass().getDeclaredField("chunkPosition");
//			if (!field.isAccessible())
//				field.setAccessible(true);
//			field.set(location, Vector3i.from(0, 0, 0));
//		}
//		catch (NoSuchFieldException | IllegalAccessException e)
//		{
//			e.printStackTrace();
//		}
//
//		when(protectionConfig.getSafeZoneWhitelists()).thenReturn(whiteList);
//		when(world.uniqueId()).thenReturn(UUID.randomUUID());
//		when(factionLogic.getFactionByChunk(world.uniqueId(), location.chunkPosition())).thenReturn(Optional.of(safezoneFaction));
//		when(itemStack.type()).thenReturn(mock(ItemType.class));
//		when(itemStack.type().toString()).thenReturn("minecraft:stone");
//		when(player.itemInHand(HandTypes.MAIN_HAND)).thenReturn(itemStack);
//		when(blockSnapshot.location()).thenReturn(Optional.of(location));
//
//		//when
//		final ProtectionResult result = protectionManager.canPlace(blockSnapshot, player.user(), false);
//
//		//then
//		assertFalse(result.hasAccess());
//	}
//}
