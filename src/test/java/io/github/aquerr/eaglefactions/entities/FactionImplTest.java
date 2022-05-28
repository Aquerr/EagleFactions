package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static org.junit.jupiter.api.Assertions.*;

class FactionImplTest
{
    private Faction faction;

    @BeforeEach
    void prepareFactionObject()
    {
        faction = FactionImpl.builder("Test", text("TE", BLUE), UUID.randomUUID())
                .build();
    }

    @Test
    void builderShouldSetFactionName()
    {
        Assertions.assertEquals("Test", faction.getName());
    }

    @Test
    void builderShouldSetFactionTag()
    {
        Assertions.assertEquals(text("TE", BLUE), faction.getTag());
    }

    @Test
    void builderBuildShouldReturnNewFactionInstance()
    {
        final Faction newFaction = faction.toBuilder().build();
        Assertions.assertNotSame(faction, newFaction);
    }

    @Test
    void builderShouldSetDefaultFlagsIfNotSpecified()
    {
        Assertions.assertEquals(PermsManager.getDefaultFactionPerms(), faction.getPerms());
    }

    @Test
    void buildingAFactionWithoutNameShouldThrowException()
    {
        //given
        //when
        //then
        assertThrows(IllegalStateException.class, () -> faction.toBuilder().setName(null).build());
    }

    @Test
    void buildingAFactionWithoutTagShouldThrowException()
    {
        //given
        //when
        //then
        assertThrows(IllegalStateException.class, () -> faction.toBuilder().setTag(null).build());
    }

    @Test
    void buildingAFactionWithoutLeaderShouldThrowException()
    {
        //given
        //when
        //then
        assertThrows(IllegalStateException.class, () -> faction.toBuilder().setLeader(null).build());
    }
}