package io.github.aquerr.eaglefactions.common.logic;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactionLogicImplTest
{
    @Mock
    private FactionsConfig factionsConfig;
    @Mock
    private StorageManager storageManager;
    @Mock
    private PlayerManager playerManager;
    @Mock
    private FactionPlayer factionPlayer;

    @InjectMocks
    @Spy
    private FactionLogicImpl factionLogic;

    @Test
    public void whenGettingFactionByNullPlayerThenThrowNullPointerException()
    {
        assertThrows(NullPointerException.class, () -> factionLogic.getFactionByPlayerUUID(null));
    }

    @Test
    public void whenGettingFactionByPlayerThenReturnFaction()
    {
        //given
        when(playerManager.getFactionPlayer(any(UUID.class))).thenReturn(Optional.of(factionPlayer));
        when(factionPlayer.getFactionName()).thenReturn(Optional.of("Test"));
        when(storageManager.getFaction("Test")).thenReturn(FactionImpl.builder("Test", Text.of("Tag"), UUID.randomUUID()).build());

        //when
        final Optional<Faction> faction = factionLogic.getFactionByPlayerUUID(UUID.randomUUID());

        //then
        assertTrue(faction.isPresent());
        verify(playerManager).getFactionPlayer(any(UUID.class));
    }

    @Test
    public void whenGettingFactionByPlayerThatIsNotInFactionThenReturnEmptyFaction()
    {
        //given
        when(playerManager.getFactionPlayer(any(UUID.class))).thenReturn(Optional.of(factionPlayer));
        when(factionPlayer.getFactionName()).thenReturn(Optional.empty());

        //when
        final Optional<Faction> faction = factionLogic.getFactionByPlayerUUID(UUID.randomUUID());

        //then
        assertFalse(faction.isPresent());
        verify(playerManager).getFactionPlayer(any(UUID.class));
    }

    @Test
    public void whenGettingFactionByNullWorldAndChunkThenThrowNullPointerException()
    {
        //given
        //when
        //then
        assertThrows(NullPointerException.class, () -> factionLogic.getFactionByChunk(null, null));
    }

    @Test
    public void whenGettingFactionByWorldAndChunkThenReturnFaction()
    {
        //given
        final UUID worldUUID = UUID.randomUUID();
        final Vector3i chunk = Vector3i.ZERO;
        final Claim claim = new Claim(worldUUID, chunk);
        final Faction faction = FactionImpl.builder("Test", Text.of("TS"), UUID.randomUUID()).setClaims(ImmutableSet.of(claim)).build();

        when(factionLogic.getFactions()).thenReturn(ImmutableMap.of("test", faction));

        //when
        final Optional<Faction> resultFaction = factionLogic.getFactionByChunk(worldUUID, chunk);

        //then
        assertTrue(resultFaction.isPresent());
        assertEquals("Test", resultFaction.get().getName());
    }

    @Test
    public void whenGettingFactionByWorldAndChunkThenReturnEmptyFaction()
    {
        //given
        final UUID worldUUID = UUID.randomUUID();
        final Vector3i chunk = Vector3i.ZERO;

        when(factionLogic.getFactions()).thenReturn(Collections.emptyMap());

        //when
        final Optional<Faction> resultFaction = factionLogic.getFactionByChunk(worldUUID, chunk);

        //then
        assertFalse(resultFaction.isPresent());
    }

    @Test
    public void whenGettingFactionByEmptyNameThenThrowException()
    {
        assertThrows(NullPointerException.class, () -> factionLogic.getFactionByName(null));
        assertThrows(IllegalArgumentException.class, () -> factionLogic.getFactionByName(""));
        assertThrows(IllegalArgumentException.class, () -> factionLogic.getFactionByName(" "));
    }
}