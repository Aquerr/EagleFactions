package io.github.aquerr.eaglefactions.common.storage.sql.h2;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.StorageConfig;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLAbstractProvider;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLProvider;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Provider extends SQLAbstractProvider implements SQLProvider
{
    private static H2Provider INSTANCE = null;

    private final Path databasePath;

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
        this.databasePath = eagleFactions.getConfigDir().resolve("data/h2/database");
        //Create database file
        final Connection connection = getConnection();
        connection.close();
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:h2:" + this.databasePath, super.getUsername(), super.getPassword());
    }

    @Override
    public String getProviderName()
    {
        return "h2";
    }
}
