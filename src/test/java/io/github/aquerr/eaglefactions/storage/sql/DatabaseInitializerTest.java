package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.util.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.aquerr.eaglefactions.storage.sql.SqlStorageTestUtils.clearDBFiles;
import static io.github.aquerr.eaglefactions.storage.sql.SqlStorageTestUtils.prepareConnectionProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(MockitoExtension.class)
class DatabaseInitializerTest
{
    private static final Path CURRENT_DIR = Paths.get(".");
    private static final String DATABASE_NAME = "eaglefactions";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

    @Mock
    private static EagleFactionsPlugin eagleFactions;

    @Nested
    class H2
    {
        @Test
        void shouldGenerateDatabase()
        {
            initializeDatabase(StorageType.H2, "localhost", 0);
        }
    }

    @Nested
    class Sqlite
    {
        @Test
        void shouldGenerateDatabase()
        {
            initializeDatabase(StorageType.SQLITE, "localhost", 0);
        }
    }

    @Nested
    class MariaDB
    {
        @Container
        private GenericContainer<?> mariaDB = new MariaDBContainer(DockerImageName.parse("mariadb:11.3.2"))
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withDatabaseName(DATABASE_NAME)
                .withExposedPorts(3306);

        @Test
        void shouldGenerateDatabase()
        {
            String host = mariaDB.getHost();
            Integer port = mariaDB.getFirstMappedPort();

            initializeDatabase(StorageType.MARIADB, host, port);
        }
    }

    @Nested
    class Mysql
    {
        @Container
        private GenericContainer<?> mysql = new MySQLContainer(DockerImageName.parse("mysql:8.3.0"))
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withDatabaseName(DATABASE_NAME)
                .withExposedPorts(3306);

        @Test
        void shouldGenerateDatabase()
        {
            String host = mysql.getHost();
            Integer port = mysql.getFirstMappedPort();

            initializeDatabase(StorageType.MYSQL, host, port);
        }
    }

    static void initializeDatabase(StorageType storageType, String host, Integer port)
    {
        String databaseUrl = "//" + host + ":" + port + "/";
        String databaseName = "eaglefactions";
        Path databaseFileDirectory = CURRENT_DIR.resolve("build").resolve("data").resolve(storageType.getName()).toAbsolutePath();

        DatabaseProperties databaseProperties = new DatabaseProperties();
        databaseProperties.setUsername(USERNAME);
        databaseProperties.setPassword(PASSWORD);
        databaseProperties.setDatabaseUrl(databaseUrl);
        databaseProperties.setDatabaseName(databaseName);
        databaseProperties.setDatabaseFileDirectory(databaseFileDirectory);

        try
        {
            try(MockedStatic<EagleFactionsPlugin> eagleFactionsStatic = mockStatic(EagleFactionsPlugin.class))
            {
                eagleFactionsStatic.when(EagleFactionsPlugin::getPlugin).thenReturn(eagleFactions);
                given(eagleFactions.getResource(any())).willAnswer(invocation -> eagleFactions.getClass().getResource(invocation.getArgument(0)).toURI());

                SQLConnectionProvider sqlConnectionProvider = prepareConnectionProvider(storageType, databaseProperties);
                DatabaseInitializer.initialize(eagleFactions, sqlConnectionProvider);

                int version = DatabaseInitializer.getDatabaseVersion(sqlConnectionProvider);

                assertThat(version).isNotZero();

                sqlConnectionProvider.close();
            }
        }
        catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
        finally
        {
            if (storageType.isFile())
            {
                clearDBFiles(databaseFileDirectory);
            }
        }
    }
}
