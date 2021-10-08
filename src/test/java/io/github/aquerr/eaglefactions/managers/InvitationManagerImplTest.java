package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InvitationManagerImplTest
{
    private static final String PLAYER_NAME = "Player Name";
    private static final String FACTION_NAME = "Faction Name";
    private static final UUID PLAYER_UUID = new UUID(4, 4);
    private static final UUID LEADER_UUID = new UUID(8, 8);

    @Mock
    private Player player;

    @Mock
    private FactionLogic factionLogic;

    @Mock
    private PlayerManager playerManager;

    @Mock
    private StorageManager storageManager;

    @InjectMocks
    private InvitationManagerImpl invitationManager;

    @Test
    public void acceptInvitationThrowsNullPointerIfFactionInviteIsNull()
    {
        assertThrows(NullPointerException.class, () -> invitationManager.acceptInvitation(null));
    }

//    @Test
//    public void acceptInvitationProperlyAcceptsInvitation()
//    {
//        FactionInvite factionInvite = new FactionInviteImpl(FACTION_NAME, PLAYER_UUID);
//        EagleFactionsPlugin.INVITE_LIST.add(factionInvite);
//        EventRunner.init(Mockito.mock(EventManager.class));
//
//        when(player.getUniqueId()).thenReturn(PLAYER_UUID);
//        when(playerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(player));
//        when(factionLogic.getFactionByName(FACTION_NAME)).thenReturn(prepareFaction());
//        when(playerManager.getFactionPlayer(PLAYER_UUID)).thenReturn(Optional.of(prepareFactionPlayer()));
//
//        invitationManager.acceptInvitation(factionInvite);
//
//        assertThat(EagleFactionsPlugin.INVITE_LIST).doesNotContain(factionInvite);
//    }

    private FactionPlayer prepareFactionPlayer()
    {
        return new FactionPlayerImpl(PLAYER_NAME, PLAYER_UUID, FACTION_NAME, 1, 1, true);
    }

    private Faction prepareFaction()
    {
        return FactionImpl.builder(FACTION_NAME, Text.of(""), LEADER_UUID)
                .build();
    }
}