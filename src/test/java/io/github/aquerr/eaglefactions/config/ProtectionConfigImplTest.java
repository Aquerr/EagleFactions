package io.github.aquerr.eaglefactions.config;

import com.google.common.collect.ImmutableSet;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Set;

import static io.github.aquerr.eaglefactions.TestUtils.BUILD_DIR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProtectionConfigImplTest
{
    @Mock
    private Configuration configuration;

    private ProtectionConfigImpl protectionConfig;

    @BeforeEach
    void setup()
    {
        given(configuration.getConfigDirectoryPath()).willReturn(BUILD_DIR);
        protectionConfig = new ProtectionConfigImpl(configuration);
    }

    @Test
    void gettingDetectedWorldNamesShouldReturnAllWorlds() throws SerializationException
    {
        // given
        final Set<String> worlds = ImmutableSet.of("ClaimableWorld", "NotClaimableWorld", "SafeZoneWorld", "WarZoneWorld");

        // when
        protectionConfig.reload();
        final Set<String> detectedWorlds = protectionConfig.getDetectedWorldNames();

        // then
        assertEquals(worlds, detectedWorlds);
    }

    @Test
    void whenItemIsWhitelistedThenReturnTrue()
    {
        ProtectionConfig.WhiteList whiteList = new ProtectionConfigImpl.WhiteListsImpl(ImmutableSet.of("minecraft:bucket"), null, null);

        //when
        final boolean result = whiteList.isItemWhiteListed("minecraft:bucket");

        assertTrue(result);
    }

    @Test
    void whenItemIsNotWhitelistedThenReturnFalse()
    {
        final ProtectionConfig.WhiteList whiteList = new ProtectionConfigImpl.WhiteListsImpl(ImmutableSet.of("minecraft:sword"), null, null);

        //when
        final boolean result = whiteList.isItemWhiteListed("minecraft:bucket");

        assertFalse(result);
    }

    @Test
    void whenWhitelistedPatternMatchesItemThenReturnTrue()
    {
        final ProtectionConfig.WhiteList whiteList = new ProtectionConfigImpl.WhiteListsImpl(ImmutableSet.of("minecraft:.*"), null, null);

        //when
        final boolean result = whiteList.isItemWhiteListed("minecraft:bucket");

        assertTrue(result);
    }

    @Test
    void whenWhitelistedPatternDoesNotMatchItemThenReturnFalse()
    {
        final ProtectionConfig.WhiteList whiteList = new ProtectionConfigImpl.WhiteListsImpl(ImmutableSet.of("enderio:.*"), null, null);

        //when
        final boolean result = whiteList.isItemWhiteListed("minecraft:bucket");

        assertFalse(result);
    }
}