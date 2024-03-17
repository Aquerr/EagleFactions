package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.storage.FactionStorage;
import io.github.aquerr.eaglefactions.storage.PlayerStorage;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.AbstractDatabaseStorageTest;
import io.github.aquerr.eaglefactions.storage.sql.FactionChestSqlHelper;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.GenericContainer;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SqliteDatabaseStorageTest extends AbstractDatabaseStorageTest
{
    @Override
    protected GenericContainer<?> buildDatabaseContainer()
    {
        return null;
    }

    @Override
    protected FactionStorage buildFactionStorage()
    {
        return new SqliteFactionStorage(
                mock(Logger.class),
                connectionProvider,
                new FactionProtectionFlagsSqliteStorageImpl(),
                mock(FactionChestSqlHelper.class)
        );
    }

    @Override
    protected PlayerStorage buildPlayerStorage()
    {
        return new SqlitePlayerStorage(connectionProvider);
    }

    @Override
    protected StorageType getStorageType()
    {
        return StorageType.SQLITE;
    }
}