package io.github.aquerr.eaglefactions.storage.sql.h2;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.SQLAbstractProvider;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class H2Provider extends SQLAbstractProvider
{
    private static H2Provider INSTANCE = null;

    private DataSource dataSource;

    public static H2Provider getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
        {
            try
            {
                INSTANCE = new H2Provider(eagleFactions);
                return INSTANCE;
            }
            catch(final SQLException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else return INSTANCE;
    }

    private H2Provider(final EagleFactions eagleFactions) throws SQLException
    {
        super(eagleFactions);
        try
        {
            Class.forName("org.h2.Driver");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Could not register H2 Driver.", e);
        }

        final Path databaseDir = eagleFactions.getConfigDir().resolve("data/h2");
        try
        {
            Files.createDirectories(databaseDir);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
        final Path databasePath = databaseDir.resolve(getDatabaseName());

        prepareHikariDataSource(databasePath);

        //Create database file
        final Connection connection = getConnection();
        connection.close();
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

    private void prepareHikariDataSource(Path databasePath)
    {
        String jdbcUrl = "jdbc:h2:file:" + databasePath;
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("url", jdbcUrl);
        config.setUsername(super.getUsername());
        config.setPassword(super.getPassword());
        config.setPoolName("eaglefactions");
        config.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(config);
    }
}
