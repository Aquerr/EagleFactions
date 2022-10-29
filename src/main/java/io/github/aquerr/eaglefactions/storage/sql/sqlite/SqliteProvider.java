package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.SQLAbstractProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.sql.SqlManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteProvider extends SQLAbstractProvider
{
    private static SqliteProvider INSTANCE = null;

    private DataSource dataSource;

    public static SqliteProvider getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
        {
            try
            {
                INSTANCE = new SqliteProvider(eagleFactions);
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

    private SqliteProvider(final EagleFactions eagleFactions) throws SQLException
    {
        super(eagleFactions);
        try
        {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("Could not register SQLite Driver.", e);
        }

        final Path databaseDir = eagleFactions.getConfigDir().resolve("data/sqlite");
        try
        {
            Files.createDirectories(databaseDir);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            throw new IllegalStateException(exception);
        }
        final Path databasePath = databaseDir.resolve(getDatabaseName() + ".db");
        prepareHikariDataSource(databasePath);
        final Connection connection = getConnection();
        connection.close();
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
        String jdbcUrl = "jdbc:sqlite:" + super.getUsername() + ":" + super.getPassword() + "@" + databasePath;
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(super.getUsername());
        config.setPassword(super.getPassword());
        config.setPoolName("eaglefactions");
        config.addDataSourceProperty("databaseName", getDatabaseName());
        config.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(config);
    }
}
