package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.storage.StorageType;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLConnectionProvider
{
    /**
     * Gets connection from the provider.
     * @return the {@link Connection}
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * Gets storage type that this provider is for.
     * @return the {@link StorageType}
     */
    StorageType getStorageType();

    /**
     * Gets database name.
     * @return the name of the database.
     */
    String getDatabaseName();

    /**
     * Used mostly in tests to shut down the underlying pool.
     */
    void close();
}
