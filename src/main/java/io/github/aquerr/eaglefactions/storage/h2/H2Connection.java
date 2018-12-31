package io.github.aquerr.eaglefactions.storage.h2;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigFields;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Connection
{
    private static final H2Connection INSTANCE = null;

    private final Path databasePath;
    private final String username;
    private final String password;
    private Connection connection;

    public static H2Connection getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new H2Connection(eagleFactions);
        else return INSTANCE;
    }

    private H2Connection(EagleFactions eagleFactions)
    {
        ConfigFields configFields = eagleFactions.getConfiguration().getConfigFields();
        this.databasePath = eagleFactions.getConfigDir().resolve("data/h2/database");
        this.username = configFields.getStorageUsername();
        this.password = configFields.getStoragePassword();
        try
        {
            connection = getConnection();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException
    {
        if (this.connection == null || this.connection.isClosed())
            return DriverManager.getConnection("jdbc:h2:" + this.databasePath, this.username, this.password);
        else return connection;
    }
}
