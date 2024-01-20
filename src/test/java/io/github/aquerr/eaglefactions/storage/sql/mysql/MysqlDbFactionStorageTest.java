package io.github.aquerr.eaglefactions.storage.sql.mysql;

import io.github.aquerr.eaglefactions.storage.FactionStorage;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorageTest;
import io.github.aquerr.eaglefactions.storage.sql.FactionChestSqlHelper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MysqlDbFactionStorageTest extends AbstractFactionStorageTest
{
    @Override
    protected String getDatabaseUrl()
    {
        String host = databaseContainer.getHost();
        Integer port = databaseContainer.getFirstMappedPort();
        return "//" + host + ":" + port + "/";
    }

    @Override
    protected GenericContainer<?> buildDatabaseContainer()
    {
        return new MySQLContainer(DockerImageName.parse("mysql:8.3.0"))
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withDatabaseName(DATABASE_NAME)
                .withExposedPorts(3306);
    }

    @Override
    protected FactionStorage buildStorage()
    {
        return new MySQLFactionStorage(
                logger,
                connectionProvider,
                new FactionProtectionFlagsMySqlStorageImpl(),
                mock(FactionChestSqlHelper.class)
        );
    }

    @Override
    protected StorageType getStorageType()
    {
        return StorageType.MYSQL;
    }
}
