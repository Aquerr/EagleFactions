package io.github.aquerr.eaglefactions.common.storage.sql.h2;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLAbstractProvider;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Provider extends SQLAbstractProvider implements SQLProvider
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
        final Path databasePath = eagleFactions.getConfigDir().resolve("data/h2/" + getDatabaseName());
        final SqlService sqlService = Sponge.getServiceManager().provideUnchecked(SqlService.class);
        this.dataSource = sqlService.getDataSource("jdbc:h2://" + super.getUsername() + ":" + super.getPassword() + "@" + databasePath);

        //Create database file
        final Connection connection = getConnection();
        connection.close();
    }

    public Connection getConnection() throws SQLException
    {
        return this.dataSource.getConnection();
    }

    @Override
    public String getProviderName()
    {
        return "h2";
    }
}
