package io.github.aquerr.eaglefactions.common.storage.sql.mysql;

import com.zaxxer.hikari.HikariDataSource;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.StorageType;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLAbstractProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLProvider extends SQLAbstractProvider
{
    private static MySQLProvider INSTANCE = null;

    private DataSource dataSource;

    public static MySQLProvider getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
        {
            try
            {
                INSTANCE = new MySQLProvider(eagleFactions);
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

    public Connection getConnection() throws SQLException
    {
        return this.dataSource.getConnection();
    }

    @Override
    public StorageType getStorageType()
    {
        return StorageType.MYSQL;
    }

    private MySQLProvider(final EagleFactions eagleFactions) throws SQLException
    {
        super(eagleFactions);

        final SqlService sqlService = Sponge.getServiceManager().provideUnchecked(SqlService.class);
        this.dataSource = sqlService.getDataSource("jdbc:mysql://" + super.getUsername() + ":" + super.getPassword() + "@" + super.getDatabaseUrl() + "?useUnicode=true&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");

        if(!databaseExists())
        {
            createDatabase();
        }

        final HikariDataSource hikariDataSource = this.dataSource.unwrap(HikariDataSource.class);
        hikariDataSource.close();
        this.dataSource = sqlService.getDataSource("jdbc:mysql://" + super.getUsername() + ":" + super.getPassword() + "@" + super.getDatabaseUrl() + super.getDatabaseName() + "?useUnicode=true&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
    }

    private boolean databaseExists() throws SQLException
    {
        try(final Connection connection = this.dataSource.getConnection(); final ResultSet resultSet = connection.getMetaData().getCatalogs())
        {
            while(resultSet.next())
            {
                if(resultSet.getString(1).equalsIgnoreCase(super.getDatabaseName()))
                {
                    resultSet.close();
                    connection.close();
                    return true;
                }
            }
        }
        return false;
    }

    private void createDatabase() throws SQLException
    {
        try(final Connection connection = this.dataSource.getConnection(); final Statement statement = connection.createStatement())
        {
            statement.execute("CREATE SCHEMA " + super.getDatabaseName() + ";");
        }
    }
}
