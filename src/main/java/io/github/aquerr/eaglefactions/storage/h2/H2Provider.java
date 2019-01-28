package io.github.aquerr.eaglefactions.storage.h2;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigFields;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Provider
{
    private static final H2Provider INSTANCE = null;

    private final Path databasePath;
    private final String username;
    private final String password;

    public static H2Provider getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new H2Provider(eagleFactions);
        else return INSTANCE;
    }

    private H2Provider(EagleFactions eagleFactions)
    {
        ConfigFields configFields = eagleFactions.getConfiguration().getConfigFields();
        this.databasePath = eagleFactions.getConfigDir().resolve("data/h2/database");
        this.username = configFields.getStorageUsername();
        this.password = configFields.getStoragePassword();
        try
        {
            //Create database file
            Connection connection = getConnection();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:h2:" + this.databasePath, this.username, this.password);
    }
}
