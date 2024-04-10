package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionInviteImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.entities.RankImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class InvitationManagerImplTest
{
    private static final String PLAYER_NAME = "Player Name";
    private static final String FACTION_NAME = "Faction Name";
    private static final UUID PLAYER_UUID = new UUID(4, 4);
    private static final UUID LEADER_UUID = new UUID(8, 8);

    @Mock
    private ServerPlayer player;
    @Mock
    private FactionLogic factionLogic;
    @Mock
    private PlayerManager playerManager;
    @Mock
    private MessageService messageService;
    @Mock
    private StorageManager storageManager;

    @InjectMocks
    private InvitationManagerImpl invitationManager;

    @Test
    void acceptInvitationThrowsNullPointerIfFactionInviteIsNull()
    {
        assertThrows(NullPointerException.class, () -> invitationManager.acceptInvitation(null));
    }

    @Test
    void acceptInvitationProperlyAcceptsInvitation()
    {
        Faction senderFaction = prepareFaction();
        FactionPlayer factionPlayer = prepareFactionPlayer();
        FactionInvite factionInvite = new FactionInviteImpl(senderFaction, factionPlayer);
        EagleFactionsPlugin.INVITE_LIST.add(factionInvite);

        given(player.uniqueId()).willReturn(PLAYER_UUID);
        given(playerManager.getPlayer(PLAYER_UUID)).willReturn(Optional.of(player));
        given(factionLogic.getFactionByName(FACTION_NAME)).willReturn(senderFaction);
        given(playerManager.getFactionPlayer(PLAYER_UUID)).willReturn(Optional.of(factionPlayer));

        try(MockedStatic<EventRunner> eventRunnerMockedStatic = mockStatic(EventRunner.class))
        {
            eventRunnerMockedStatic.when(() -> EventRunner.runFactionJoinEventPre(any(), any()))
                    .thenReturn(false);
            eventRunnerMockedStatic.when(() -> EventRunner.runFactionJoinEventPost(any(), any()))
                    .thenReturn(false);

            invitationManager.acceptInvitation(factionInvite);

            assertThat(EagleFactionsPlugin.INVITE_LIST).doesNotContain(factionInvite);
        }
    }

    private FactionPlayer prepareFactionPlayer()
    {
        return new FactionPlayerImpl(PLAYER_NAME, PLAYER_UUID, FACTION_NAME, 1, 1, true);
    }

    private Faction prepareFaction()
    {
        return FactionImpl.builder(FACTION_NAME, Component.text(""))
                .leader(LEADER_UUID)
                .ranks(List.of(RankImpl.builder().name("default").build()))
                .build();
    }
}