package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.FactionType;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtectionManagerTest
{
	@Mock
	private FactionLogic factionLogic;
	@Mock
	private PermsManager permsManager;
	@Mock
	private PlayerManager playerManager;
	@Mock
	private MessageService messageService;
	@Mock
	private ProtectionConfig protectionConfig;
	@Mock
	private ChatConfig chatConfig;
	@Mock
	private FactionsConfig factionsConfig;

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
		final ServerPlayer player = mock(ServerPlayer.class);
		final Entity entity = mock(Entity.class);
		final User user = mock(User.class);
		when(player.user()).thenReturn(user);
		when(playerManager.hasAdminMode(player.user())).thenReturn(true);

		//when
		final boolean result = protectionManager.canHitEntity(entity, player, false).hasAccess();

		//then
		assertTrue(result);
	}
}
