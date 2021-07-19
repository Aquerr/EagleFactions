package io.github.aquerr.eaglefactions.config;

import com.google.common.collect.ImmutableSet;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProtectionConfigImplTest
{
    @Mock
    private Configuration configuration;
    @Mock
    private ConfigurationLoader configurationLoader;
    @Mock
    private CommentedConfigurationNode configurationNode;

//    @InjectMocks
//    @Spy
//    private ProtectionConfigImpl protectionConfig;

//    @Test
//    public void gettingDetectedWorldNamesShouldReturnAllWorlds()
//    {
//        given
//        final Set<String> worlds = ImmutableSet.of("ClaimableWorld", "NotClaimableWorld", "SafeZoneWorld", "WarZoneWorld");
//        doReturn("ClaimableWorld").when(protectionConfig.getClaimableWorldNames());
//        doReturn("NotClaimableWorld").when(protectionConfig.getNotClaimableWorldNames());
//        doReturn("SafeZoneWorld").when(protectionConfig.getSafeZoneWorldNames());
//        doReturn("WarZoneWorld").when(protectionConfig.getWarZoneWorldNames());
//
//        when
//        final Set<String> detectedWorlds = protectionConfig.getDetectedWorldNames();
//
//        then
//        assertEquals(worlds, detectedWorlds);
//        verify(protectionConfig, times(1)).getClaimableWorldNames();
//        verify(protectionConfig, times(1)).getNotClaimableWorldNames();
//        verify(protectionConfig, times(1)).getSafeZoneWorldNames();
//        verify(protectionConfig, times(1)).getWarZoneWorldNames();
//    }

    @Test
    public void whenItemIsWhitelistedThenReturnTrue()
    {
        ProtectionConfig.WhiteList whiteList = new ProtectionConfigImpl.WhiteListsImpl(ImmutableSet.of("minecraft:bucket"), null, null);

        //when
        final boolean result = whiteList.isItemWhiteListed("minecraft:bucket");

        assertTrue(result);
    }

    @Test
    public void whenItemIsNotWhitelistedThenReturnFalse()
    {
        final ProtectionConfig.WhiteList whiteList = new ProtectionConfigImpl.WhiteListsImpl(ImmutableSet.of("minecraft:sword"), null, null);

        //when
        final boolean result = whiteList.isItemWhiteListed("minecraft:bucket");

        assertFalse(result);
    }

    @Test
    public void whenWhitelistedPatternMatchesItemThenReturnTrue()
    {
        final ProtectionConfig.WhiteList whiteList = new ProtectionConfigImpl.WhiteListsImpl(ImmutableSet.of("minecraft:.*"), null, null);

        //when
        final boolean result = whiteList.isItemWhiteListed("minecraft:bucket");

        assertTrue(result);
    }

    @Test
    public void whenWhitelistedPatternDoesNotMatchItemThenReturnFalse()
    {
        final ProtectionConfig.WhiteList whiteList = new ProtectionConfigImpl.WhiteListsImpl(ImmutableSet.of("enderio:.*"), null, null);

        //when
        final boolean result = whiteList.isItemWhiteListed("minecraft:bucket");

        assertFalse(result);
    }
}