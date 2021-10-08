package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.SQLAbstractProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

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

    private final DataSource dataSource;

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
        final Path databaseDir = eagleFactions.getConfigDir().resolve("data/sqlite");
        try
        {
            Files.createDirectories(databaseDir);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
        final Path databasePath = databaseDir.resolve(getDatabaseName() + ".db");
        final SqlService sqlService = Sponge.getServiceManager().provideUnchecked(SqlService.class);
        this.dataSource = sqlService.getDataSource("jdbc:sqlite://" + super.getUsername() + ":" + super.getPassword() + "@" + databasePath);
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
}