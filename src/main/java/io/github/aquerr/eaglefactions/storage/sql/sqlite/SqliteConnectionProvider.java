package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.DatabaseProperties;
import io.github.aquerr.eaglefactions.storage.sql.SQLAbstractConnectionProvider;
import io.github.aquerr.eaglefactions.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteConnectionProvider extends SQLAbstractConnectionProvider
{

    private HikariDataSource dataSource;

    public SqliteConnectionProvider(final DatabaseProperties properties)
    {
        super(properties);

        final Path databaseDir = properties.getDatabaseFileDirectory();
        try
        {
            FileUtils.createDirectoryIfNotExists(databaseDir);

            String databaseName = getDatabaseName();
            String databaseNameWithExtension = databaseName.endsWith(".db") ? databaseName : databaseName + ".db";

            final Path databasePath = databaseDir.resolve(databaseNameWithExtension);
            prepareHikariDataSource(databasePath);
        }
        catch (IOException exception)
        {
            throw new IllegalStateException(exception);
        }
    }

    public Connection getConnection() throws SQLException
    {
        Connection connection = this.dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("PRAGMA foreign_keys = ON;");
        statement.close();
        return connection;
    }

    @Override
    public StorageType getStorageType()
    {
        return StorageType.SQLITE;
    }

    private void prepareHikariDataSource(Path databasePath)
    {
        String jdbcUrl = "jdbc:sqlite:" + databasePath;
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        config.addDataSourceProperty("url", jdbcUrl);
        config.setUsername(super.getUsername());
        config.setPassword(super.getPassword());
        config.setPoolName("eaglefactions");
        config.setMaximumPoolSize(2);
        this.dataSource = new HikariDataSource(config);
    }

    public void close()
    {
        this.dataSource.close();
    }
}
