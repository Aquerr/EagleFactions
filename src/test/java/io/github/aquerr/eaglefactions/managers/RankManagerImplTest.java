package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionMemberImpl;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagImpl;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.math.vector.Vector3i;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RankManagerImplTest
{
    private static final UUID PLAYER_UUID_1 = UUID.randomUUID();
    private static final UUID PLAYER_UUID_2 = UUID.randomUUID();

    @Test
    void getEditableRanksShouldReturnCorrectRanksForLeader()
    {
        Faction faction = prepareFaction("test_faction", PLAYER_UUID_1, PLAYER_UUID_2);

        List<Rank> defaultRanks = RankManagerImpl.getDefaultRanks();
        List<Rank> ranks = RankManagerImpl.getEditableRanks(faction, PLAYER_UUID_1, false);

        assertThat(ranks).containsExactlyElementsOf(defaultRanks.stream()
                .filter(rank -> !rank.getName().equalsIgnoreCase("leader"))
                .collect(Collectors.toList()));
    }

    @Test
    void getEditableRanksShouldReturnCorrectRanksForAdmin()
    {
        Faction faction = prepareFaction("test_faction", PLAYER_UUID_1, PLAYER_UUID_2);

        List<Rank> defaultRanks = RankManagerImpl.getDefaultRanks();
        List<Rank> ranks = RankManagerImpl.getEditableRanks(faction, PLAYER_UUID_1, true);

        assertThat(ranks).containsExactlyElementsOf(defaultRanks.stream()
                .filter(rank -> !rank.getName().equalsIgnoreCase("leader"))
                .collect(Collectors.toList()));
    }

    @Test
    void getEditableRanksShouldReturnCorrectRanksForMiddleRank()
    {
        Faction faction = prepareFaction("test_faction", PLAYER_UUID_1, PLAYER_UUID_2);

        List<Rank> ranks = RankManagerImpl.getEditableRanks(faction, PLAYER_UUID_2, false);

        assertThat(ranks).noneMatch(rank -> rank.getName().equalsIgnoreCase("leader") || rank.getName().equalsIgnoreCase("officer"));
    }

    protected Faction prepareFaction(String factionName, UUID leaderUUID, UUID memberUUID)
    {
        return FactionImpl.builder(factionName, Component.text("TE"))
                .leader(leaderUUID)
                .description("test_desc")
                .messageOfTheDay("test_motd")
                .members(Set.of(new FactionMemberImpl(leaderUUID, Set.of("leader")),
                        new FactionMemberImpl(memberUUID, Set.of("recruit", "officer"))))
                .alliancePermissions(Set.of(FactionPermission.BLOCK_DESTROY))
                .trucePermissions(Set.of(FactionPermission.BLOCK_PLACE))
                .isPublic(true)
                .lastOnline(LocalDateTime.of(2024, 3, 11, 18, 10).toInstant(ZoneOffset.UTC))
                .createdDate(LocalDateTime.of(2024, 1, 4, 12, 15).toInstant(ZoneOffset.UTC))
                .ranks(RankManagerImpl.getDefaultRanks())
                .alliances(Set.of("test_alliance"))
                .truces(Set.of("test_truce"))
                .enemies(Set.of("test_enemy"))
                .protectionFlags(Set.of(new ProtectionFlagImpl(ProtectionFlagType.PVP, true)))
                .home(null)
                .claims(Set.of(new Claim(UUID.randomUUID(), Vector3i.ONE), new Claim(UUID.randomUUID(), Vector3i.ZERO)))
                .chest(new FactionChestImpl(factionName))
                .build();
    }
}