package io.github.aquerr.eaglefactions.storage.sql.h2;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.storage.sql.SQLProvider;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Provider implements SQLProvider
{
    private static H2Provider INSTANCE = null;

    private final Path databasePath;
    private final String username;
    private final String password;

    public static H2Provider getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
        {
            try
            {
                INSTANCE = new H2Provider(eagleFactions);
                return INSTANCE;
            }
            catch(SQLException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else return INSTANCE;
    }

    private H2Provider(final EagleFactions eagleFactions) throws SQLException
    {
        ConfigFields configFields = eagleFactions.getConfiguration().getConfigFields();
        this.databasePath = eagleFactions.getConfigDir().resolve("data/h2/database");
        this.username = configFields.getStorageUsername();
        this.password = configFields.getStoragePassword();
        //Create database file
        Connection connection = getConnection();
        connection.close();
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:h2:" + this.databasePath, this.username, this.password);
    }

    @Override
    public String getProviderName()
    {
        return "h2";
    }
}
