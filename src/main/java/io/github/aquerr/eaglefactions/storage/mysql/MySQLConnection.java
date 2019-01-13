package io.github.aquerr.eaglefactions.storage.mysql;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigFields;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection
{
    private static final MySQLConnection INSTANCE = null;

    private final String databaseUrl;
    private final String databaseName;
    private final String username;
    private final String password;

    public static MySQLConnection getInstance(EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new MySQLConnection(eagleFactions);
        else return INSTANCE;
    }

    private MySQLConnection(EagleFactions eagleFactions)
    {
        try
        {
            //Load MySQL driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch(InstantiationException | IllegalAccessException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        ConfigFields configFields = eagleFactions.getConfiguration().getConfigFields();
        this.databaseUrl = configFields.getDatabaseUrl();
        this.databaseName = configFields.getDatabaseName();
        this.username = configFields.getStorageUsername();
        this.password = configFields.getStoragePassword();
        try
        {
            Connection connection = openConnection();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Connection openConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:mysql://" + this.databaseUrl + this.databaseName, this.username, this.password);
    }
}
