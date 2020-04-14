package io.github.aquerr.eaglefactions.common.storage.sql.mysql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLAbstractProvider;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLProvider;

import java.sql.*;

public class MySQLProvider extends SQLAbstractProvider implements SQLProvider
{
    private static MySQLProvider INSTANCE = null;

//    private static final String CONNECTION_OPTIONS = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&&disableMariaDbDriver";
    private static final String CONNECTION_OPTIONS = "&useUnicode=true&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    public static MySQLProvider getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
        {
            try
            {
                INSTANCE = new MySQLProvider(eagleFactions);
                return INSTANCE;
            }
            catch(final IllegalAccessException | InstantiationException | ClassNotFoundException | SQLException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else return INSTANCE;
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:mysql://" + super.getDatabaseUrl() + super.getDatabaseName() + "?user=" + super.getUsername() + "&password=" + super.getPassword() + CONNECTION_OPTIONS);
    }

    @Override
    public String getProviderName()
    {
        return "mysql";
    }

    private MySQLProvider(final EagleFactions eagleFactions) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException
    {
        super(eagleFactions);
        //Load MySQL driver
//        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

        if(!databaseExists())
            createDatabase();
    }

    private boolean databaseExists() throws SQLException
    {
        //Connection connection = DriverManager.getConnection("jdbc:mysql://" + this.username + ":" + this.password + "@" + this.databaseUrl + this.databaseName);
//        Connection connection = DriverManager.getConnection("jdbc:mysql://" + super.getDatabaseUrl() + "?user=" + super.getUsername() + "&password=" + super.getPassword() + CONNECTION_OPTIONS);
        final Connection connection = DriverManager.getConnection("jdbc:mysql://" + super.getDatabaseUrl(), super.getUsername(),super.getPassword());
        final ResultSet resultSet = connection.getMetaData().getCatalogs();

        while(resultSet.next())
        {
            if(resultSet.getString(1).equalsIgnoreCase(super.getDatabaseName()))
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
        final Connection connection = DriverManager.getConnection("jdbc:mysql://" + super.getDatabaseUrl() + "?user=" + super.getUsername() + "&password=" + super.getPassword() + CONNECTION_OPTIONS);
        final Statement statement = connection.createStatement();
        statement.execute("CREATE SCHEMA " + super.getDatabaseName() + ";");
        statement.close();
        connection.close();
    }
}
