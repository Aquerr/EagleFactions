package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.h2.H2ConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.mariadb.MariaDbConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.mysql.MySQLConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.sqlite.SqliteConnectionProvider;
import io.github.aquerr.eaglefactions.util.FileUtils;

import java.nio.file.Path;

class SqlStorageTestUtils
{
    public static <T extends SQLConnectionProvider> T prepareConnectionProvider(StorageType storageType, DatabaseProperties databaseProperties)
    {
        switch (storageType)
        {
            case H2:
                return (T)new H2ConnectionProvider(databaseProperties);
            case MYSQL:
                return (T)new MySQLConnectionProvider(databaseProperties);
            case SQLITE:
                return (T)new SqliteConnectionProvider(databaseProperties);
            case MARIADB:
                return (T)new MariaDbConnectionProvider(databaseProperties);
            default:
                throw new IllegalArgumentException("Bad storage type = " + storageType);
        }
    }

    public static void clearDBFiles(Path databaseDirectory)
    {
        FileUtils.deleteDirectoryRecursive(databaseDirectory.toFile());
    }

}
