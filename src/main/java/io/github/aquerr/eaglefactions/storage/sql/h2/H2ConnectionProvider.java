package io.github.aquerr.eaglefactions.storage.sql.h2;

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

public class H2ConnectionProvider extends SQLAbstractConnectionProvider
{
    private HikariDataSource dataSource;

    public H2ConnectionProvider(final DatabaseProperties properties)
    {
        super(properties);

        final Path databaseDir = properties.getDatabaseFileDirectory();
        try
        {
            FileUtils.createDirectoryIfNotExists(databaseDir);

            final Path databasePath = databaseDir.resolve(getDatabaseName());

            prepareHikariDataSource(databasePath);
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    public Connection getConnection() throws SQLException
    {
        return this.dataSource.getConnection();
    }

    @Override
    public StorageType getStorageType()
    {
        return StorageType.H2;
    }

    @Override
    public void close()
    {
        this.dataSource.close();
    }

    private void prepareHikariDataSource(Path databasePath)
    {
        String jdbcUrl = "jdbc:h2:file:" + databasePath + ";AUTO_SERVER=TRUE";
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("url", jdbcUrl);
        config.setUsername(super.getUsername());
        config.setPassword(super.getPassword());
        config.setPoolName("eaglefactions");
        config.setMaximumPoolSize(2);
        this.dataSource = new HikariDataSource(config);
    }
}
