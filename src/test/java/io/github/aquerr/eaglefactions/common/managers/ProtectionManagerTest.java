package io.github.aquerr.eaglefactions.common.managers;

import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.FlagManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
	private FlagManager flagManager;

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
}
