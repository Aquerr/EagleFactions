package io.github.aquerr.eaglefactions.common.managers;

import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

	@InjectMocks
	private ProtectionManagerImpl protectionManager;

	@Test
	void bucketShouldBeWhitelisted()
	{
		//given
		final Set<String> items = new HashSet<>();
		items.add("minecraft:bucket");
		when(protectionConfig.getWhiteListedItems()).thenReturn(items);
		//when

		final boolean result = protectionManager.isItemWhitelisted("minecraft:bucket");

		//then
		verify(protectionConfig).getWhiteListedItems();

		assertTrue(result);
	}

	@Test
	void bucketShouldNotBeWhitelisted()
	{
		final Set<String> items = new HashSet<>();
		items.add("minecraft:bucket");

		when(protectionConfig.getWhiteListedItems()).thenReturn(Collections.emptySet());

		//then

		final boolean result = protectionManager.isItemWhitelisted("minecraft:bucket");

		verify(protectionConfig).getWhiteListedItems();
		assertFalse(result);
	}

	@Test
	void stoneShouldBeWhiteListedForPlaceAndDestroy()
	{
		//given
		final Set<String> items = new HashSet<>();
		items.add("minecraft:stone");
		//when

		when(protectionConfig.getWhiteListedPlaceDestroyBlocks()).thenReturn(items);

		//then

		final boolean result = protectionManager.isBlockWhitelistedForPlaceDestroy("minecraft:stone");

		verify(protectionConfig).getWhiteListedPlaceDestroyBlocks();
		assertTrue(result);
	}

	@Test
	void stoneShouldNotBeWhiteListedForPlaceAndDestroy()
	{
		//given
		//when
		when(protectionConfig.getWhiteListedPlaceDestroyBlocks()).thenReturn(Collections.emptySet());

		//then

		final boolean result = protectionManager.isBlockWhitelistedForPlaceDestroy("minecraft:stone");

		verify(protectionConfig).getWhiteListedPlaceDestroyBlocks();
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
		final boolean result = protectionManager.canHitEntity(entity, player, false);
		assertTrue(result);
	}
}
