package io.github.aquerr.eaglefactions.storage.mysql;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigFields;

import javax.management.relation.RelationSupport;
import java.sql.*;

public class MySQLConnection
{
    private static final MySQLConnection INSTANCE = null;

    private static final String TIME_ZONE_PROPERTY = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

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
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
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
            if(!databaseExists())
                createDatabase();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private boolean databaseExists() throws SQLException
    {
        Connection connection = DriverManager.getConnection("jdbc:mysql://" + this.databaseUrl + TIME_ZONE_PROPERTY, this.username, this.password);
//        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=sa&password=admin&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
        ResultSet resultSet = connection.getMetaData().getCatalogs();

        while(resultSet.next())
        {
            if(resultSet.getString(1).equalsIgnoreCase(this.databaseName))
                return true;
        }
        resultSet.close();
        connection.close();
        return false;
    }

    private void createDatabase() throws SQLException
    {
        Connection connection = openConnection();
        Statement statement = connection.createStatement();
        statement.execute("create database " + this.databaseName + ";");
        statement.close();
        connection.commit();
        connection.close();
    }

    public Connection openConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:mysql://" + this.databaseUrl + this.databaseName + TIME_ZONE_PROPERTY, this.username, this.password);
    }
}
