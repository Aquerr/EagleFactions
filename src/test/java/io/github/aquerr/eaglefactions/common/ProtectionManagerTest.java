package io.github.aquerr.eaglefactions.common;

import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.common.managers.ProtectionManagerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;

public class ProtectionManagerTest
{
	private static ProtectionConfig protectionConfig;
	private static ProtectionManager protectionManager;

	@BeforeAll
	static void prepare()
	{
		protectionConfig = Mockito.mock(ProtectionConfig.class);
		protectionManager = Mockito.mock(ProtectionManager.class);
	}

//	@Test
//	void isItemWhitelistedShouldReturnTrueForBucket()
//	{
//		Mockito.when(protectionConfig.getWhiteListedItems()).thenReturn(new HashSet<>(Collections.singletonList("minecraft:bucket")));
//
//		Field[] fields = protectionManager.getClass().getFields();
//		Field[] declaredFields = protectionManager.getClass().getDeclaredFields();
//
//		Mockito.spy()
//
//		try
//		{
//			protectionManager.getClass().getDeclaredField("protectionConfig").set(protectionManager, protectionConfig);
//		}
//		catch(IllegalAccessException e)
//		{
//			e.printStackTrace();
//		}
//		catch(NoSuchFieldException e)
//		{
//			e.printStackTrace();
//		}
//
//		Assertions.assertTrue(protectionManager.isItemWhitelisted("minecraft:bucket"));
//	}
}
