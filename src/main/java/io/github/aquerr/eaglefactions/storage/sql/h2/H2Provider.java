package io.github.aquerr.eaglefactions.storage.sql.h2;

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

public class H2Provider extends SQLAbstractProvider
{
    private static H2Provider INSTANCE = null;

    private final DataSource dataSource;

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
        final SqlManager sqlManager = Sponge.sqlManager();
        this.dataSource = sqlManager.dataSource("jdbc:h2:" + super.getUsername() + ":" + super.getPassword() + "@" + databasePath);

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
}
