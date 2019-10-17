package io.github.aquerr.eaglefactions.common.storage.sql.mysql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLProvider;

import java.sql.*;

public class MySQLProvider implements SQLProvider
{
    private static MySQLProvider INSTANCE = null;

    private static final String CONNECTION_OPTIONS = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&&disableMariaDbDriver";

    private final String databaseUrl;
    private final String databaseName;
    private final String username;
    private final String password;

    public static MySQLProvider getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
        {
            try
            {
                INSTANCE = new MySQLProvider(eagleFactions);
                return INSTANCE;
            }
            catch(IllegalAccessException | InstantiationException | ClassNotFoundException | SQLException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else return INSTANCE;
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:mysql://" + this.databaseUrl + this.databaseName + CONNECTION_OPTIONS, this.username, this.password);
    }

    @Override
    public String getProviderName()
    {
        return "mysql";
    }

    private MySQLProvider(final EagleFactions eagleFactions) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException
    {
        //Load MySQL driver
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

        ConfigFields configFields = eagleFactions.getConfiguration().getConfigFields();
        this.databaseUrl = configFields.getDatabaseUrl();
        this.databaseName = configFields.getDatabaseName();
        this.username = configFields.getStorageUsername();
        this.password = configFields.getStoragePassword();
        if(!databaseExists())
            createDatabase();
    }

    private boolean databaseExists() throws SQLException
    {
        //Connection connection = DriverManager.getConnection("jdbc:mysql://" + this.username + ":" + this.password + "@" + this.databaseUrl + this.databaseName);
        Connection connection = DriverManager.getConnection("jdbc:mysql://" + this.databaseUrl + "?user=" + this.username + "&password=" + this.password + CONNECTION_OPTIONS);
        final ResultSet resultSet = connection.getMetaData().getCatalogs();

        while(resultSet.next())
        {
            if(resultSet.getString(1).equalsIgnoreCase(this.databaseName))
            {
                resultSet.close();
                connection.close();
                return true;
            }
        }
        resultSet.close();
        connection.close();
        return false;
    }

    private void createDatabase() throws SQLException
    {
        Connection connection = DriverManager.getConnection("jdbc:mysql://" + this.databaseUrl + "?user=" + this.username + "&password=" + this.password + CONNECTION_OPTIONS);
        Statement statement = connection.createStatement();
        statement.execute("CREATE SCHEMA " + this.databaseName + ";");
        statement.close();
        connection.close();
    }
}
