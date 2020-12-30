package io.github.aquerr.eaglefactions.common.logic;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FactionLogicImplTest
{
    private static final String FACTION_NAME = "Test Faction";
    private static final Text FACTION_TAG = Text.of("TF");
    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final String PLAYER_NAME = "Test Player Name";

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

    @Test
    void getFactionByPlayerUUIDThrowsNullPointerExceptionWhenPlayerUUIDIsNull()
    {
        assertThrows(NullPointerException.class, () -> this.factionLogic.getFactionByPlayerUUID(null));
    }

    @Test
    void getFactionByPlayerUUIDThatIsInFactionShouldReturnFaction()
    {
        when(factionPlayer.getFactionName()).thenReturn(Optional.of(FACTION_NAME));
        when(this.playerManager.getFactionPlayer(PLAYER_UUID)).thenReturn(Optional.of(factionPlayer));
        when(this.storageManager.getFaction(FACTION_NAME)).thenReturn(prepareFaction());

        final Optional<Faction> faction = this.factionLogic.getFactionByPlayerUUID(PLAYER_UUID);

        assertTrue(faction.isPresent());
        assertEquals(FACTION_NAME, faction.get().getName());
        assertEquals(FACTION_TAG, faction.get().getTag());
        assertTrue(faction.get().containsPlayer(PLAYER_UUID));
    }

    @Test
    void changeTagCallsSaveFaction()
    {
        this.factionLogic.changeTag(prepareFaction(), "NewTag");
        verify(this.storageManager, times(1)).saveFaction(any(Faction.class));
    }

    @Test
    void changeTagThrowsExceptionIfFactionIsNullOrTagIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> this.factionLogic.changeTag(null, " "));
        assertThrows(NullPointerException.class, () -> this.factionLogic.changeTag(prepareFaction(), null));
        assertThrows(IllegalArgumentException.class, () -> this.factionLogic.changeTag(prepareFaction(), ""));
    }

    @Test
    void setChestCallsSaveFaction()
    {
        this.factionLogic.setChest(prepareFaction(), new FactionChestImpl("test"));
        verify(this.storageManager, times(1)).saveFaction(any(Faction.class));
    }

    @Test
    void setChestThrowsNullPointerExceptionIfFactionOrChestIsNull()
    {
        assertThrows(NullPointerException.class, () -> this.factionLogic.setChest(null, null));
        assertThrows(NullPointerException.class, () -> this.factionLogic.setChest(prepareFaction(), null));
    }

    @Test
    void setDescriptionThrowsNullPointerExceptionIfFactionOrDescriptionIsNull()
    {
        assertThrows(NullPointerException.class, () -> this.factionLogic.setDescription(null, ""));
        assertThrows(NullPointerException.class, () -> this.factionLogic.setDescription(prepareFaction(), null));
    }

    @Test
    void setDescriptionCallsSaveFaction()
    {
        this.factionLogic.setDescription(prepareFaction(), "This is my custom description");
        verify(this.storageManager, times(1)).saveFaction(any(Faction.class));
    }

    @Test
    void setMessageOfTheDayCallsSaveFaction()
    {
        this.factionLogic.setMessageOfTheDay(prepareFaction(), "This is my custom motd");
        verify(this.storageManager, times(1)).saveFaction(any(Faction.class));
    }

    @Test
    void setMessageOfTheDayThrowsNullPointerExceptionIfFactionOrMotdIsNull()
    {
        assertThrows(NullPointerException.class, () -> this.factionLogic.setMessageOfTheDay(null, ""));
        assertThrows(NullPointerException.class, () -> this.factionLogic.setMessageOfTheDay(prepareFaction(), null));
    }

    @Test
    void setIsPublicThrowsNullPointerExceptionIfFactionIsNull()
    {
        assertThrows(NullPointerException.class, () -> this.factionLogic.setIsPublic(null, true));
    }

    @Test
    void setIsPublicCallsSaveFaction()
    {
        this.factionLogic.setIsPublic(prepareFaction(), true);
        verify(this.storageManager, times(1)).saveFaction(any(Faction.class));
    }

    private Faction prepareFaction()
    {
        return FactionImpl.builder(FACTION_NAME, FACTION_TAG, PLAYER_UUID).build();
    }
}