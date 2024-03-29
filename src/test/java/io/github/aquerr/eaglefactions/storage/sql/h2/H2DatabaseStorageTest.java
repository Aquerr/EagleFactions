package io.github.aquerr.eaglefactions.storage.sql.h2;

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
class H2DatabaseStorageTest extends AbstractDatabaseStorageTest
{
    @Override
    protected GenericContainer<?> buildDatabaseContainer()
    {
        return null;
    }

    @Override
    protected FactionStorage buildFactionStorage()
    {
        return new H2FactionStorage(
                mock(Logger.class),
                connectionProvider,
                new FactionProtectionFlagsH2StorageImpl(),
                mock(FactionChestSqlHelper.class)
        );
    }

    @Override
    protected PlayerStorage buildPlayerStorage()
    {
        return new H2PlayerStorage(connectionProvider);
    }

    @Override
    protected StorageType getStorageType()
    {
        return StorageType.H2;
    }
}