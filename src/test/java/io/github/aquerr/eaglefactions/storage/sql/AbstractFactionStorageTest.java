package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionMemberImpl;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagImpl;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.storage.FactionStorage;
import io.github.aquerr.eaglefactions.storage.StorageType;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.spongepowered.math.vector.Vector3i;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractFactionStorageTest
{
    protected static final Path BUILD_DIR = Paths.get(".").resolve("build");
    protected static final String DATABASE_NAME = "eaglefactions";
    protected static final String USERNAME = "admin";
    protected static final String PASSWORD = "password";

    protected GenericContainer<?> databaseContainer;

    protected SQLConnectionProvider connectionProvider;

    private FactionStorage factionStorage;

    protected abstract GenericContainer<?> buildDatabaseContainer();

    protected abstract FactionStorage buildStorage();

    protected abstract StorageType getStorageType();

    protected String getDatabaseUrl()
    {
        return null;
    }

    protected Logger logger = LogManager.getLogger("test-logger");

    @BeforeAll
    void setUp() throws SQLException, IOException
    {
        this.databaseContainer = buildDatabaseContainer();
        if (databaseContainer != null)
        {
            databaseContainer.setStartupCheckStrategy(new IsRunningStartupCheckStrategy()
                    .withTimeout(Duration.of(60, ChronoUnit.SECONDS)));
            databaseContainer.close();
            databaseContainer.start();
        }

        connectionProvider = SqlStorageTestUtils.prepareConnectionProvider(
                getStorageType(),
                prepareDatabaseProperties()
        );

        // Initialize DB
        EagleFactionsPlugin eagleFactions = mock(EagleFactionsPlugin.class);
        try(MockedStatic<EagleFactionsPlugin> eagleFactionsStatic = mockStatic(EagleFactionsPlugin.class))
        {
            eagleFactionsStatic.when(EagleFactionsPlugin::getPlugin).thenReturn(eagleFactions);
            given(eagleFactions.getResource(any())).willAnswer(invocation -> eagleFactions.getClass().getResource(invocation.getArgument(0)).toURI());

            DatabaseInitializer.initialize(eagleFactions, connectionProvider);
        }

        this.factionStorage = buildStorage();
    }

    @Test
    @Order(1)
    void shouldSaveFaction()
    {
        boolean didSave = factionStorage.saveFaction(prepareFaction("test_faction"));
        assertThat(didSave).isTrue();
    }

    @Test
    @Order(2)
    void shouldGetFaction()
    {
        Faction expectedFaction = prepareFaction("new_faction");
        factionStorage.saveFaction(expectedFaction);
        Faction actual = factionStorage.getFaction("new_faction");
        assertThat(actual).isNotNull();
        assertThat(actual).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedFaction);
    }

    @Test
    @Order(3)
    void shouldDeleteFaction()
    {
        factionStorage.deleteFaction("test_faction");
        assertThat(factionStorage.getFactions()).hasSize(1);
        factionStorage.deleteFactions();
        assertThat(factionStorage.getFactions()).isEmpty();
    }

    @AfterAll
    void cleanUp()
    {
        connectionProvider.close();
        if (databaseContainer != null)
        {
            databaseContainer.close();
        }
        if (connectionProvider.getStorageType().isFile())
        {
            SqlStorageTestUtils.clearDBFiles(buildDatabaseDir());
        }
    }

    private DatabaseProperties prepareDatabaseProperties()
    {
        DatabaseProperties databaseProperties = new DatabaseProperties();
        databaseProperties.setUsername(USERNAME);
        databaseProperties.setPassword(PASSWORD);
        databaseProperties.setDatabaseUrl(getDatabaseUrl());
        databaseProperties.setDatabaseName(DATABASE_NAME);
        databaseProperties.setDatabaseFileDirectory(buildDatabaseDir());
        return databaseProperties;
    }

    private Path buildDatabaseDir()
    {
        return BUILD_DIR.resolve("data")
                .resolve(getStorageType().getName())
                .toAbsolutePath();
    }

    protected Faction prepareFaction(String factionName)
    {
        return FactionImpl.builder(factionName, Component.text("TE"), UUID.randomUUID())
                .description("test_desc")
                .messageOfTheDay("test_motd")
                .members(Set.of(new FactionMemberImpl(UUID.randomUUID(), Set.of("recruit", "officer"))))
                .alliancePermissions(Set.of(FactionPermission.BLOCK_DESTROY))
                .trucePermissions(Set.of(FactionPermission.BLOCK_PLACE))
                .isPublic(true)
                .lastOnline(LocalDateTime.of(2024, 3, 11, 18, 10).toInstant(ZoneOffset.UTC))
                .createdDate(LocalDateTime.of(2024, 1, 4, 12, 15).toInstant(ZoneOffset.UTC))
                .ranks(RankManagerImpl.getDefaultRanks())
                .defaultRankName("recruit")
                .alliances(Set.of("test_alliance"))
                .truces(Set.of("test_truce"))
                .enemies(Set.of("test_enemy"))
                .protectionFlags(Set.of(new ProtectionFlagImpl(ProtectionFlagType.PVP, true)))
                .home(null)
                .claims(Set.of(new Claim(UUID.randomUUID(), Vector3i.ONE), new Claim(UUID.randomUUID(), Vector3i.ZERO)))
                .chest(new FactionChestImpl(factionName))
                .defaultRankName(RankManagerImpl.buildDefaultRecruitRank().getName())
                .build();
    }
}
