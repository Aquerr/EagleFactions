package io.github.aquerr.eaglefactions.common.storage.sql.mariadb;

import com.zaxxer.hikari.HikariDataSource;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLAbstractProvider;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.sql.DataSource;
import java.sql.*;

public class MariaDbProvider extends SQLAbstractProvider implements SQLProvider
{
	private static MariaDbProvider INSTANCE = null;

	private DataSource dataSource;

	public static MariaDbProvider getInstance(final EagleFactions eagleFactions)
	{
		if (INSTANCE == null)
		{
			try
			{
				INSTANCE = new MariaDbProvider(eagleFactions);
			}
			catch(final SQLException e)
			{
				Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "Error Code: " + e.getErrorCode() + " | SQL State: " + e.getSQLState() + " | Error Message: " + e.getMessage()));
				e.printStackTrace();
				return null;
			}
		}
		return INSTANCE;
	}

	public Connection getConnection() throws SQLException
	{
		return this.dataSource.getConnection();
	}

	@Override
	public String getProviderName()
	{
		return "mariadb";
	}

	private MariaDbProvider(final EagleFactions eagleFactions) throws SQLException
	{
		super(eagleFactions);
		final SqlService sqlService = Sponge.getServiceManager().provideUnchecked(SqlService.class);
		this.dataSource = sqlService.getDataSource("jdbc:h2://" + super.getUsername() + ":" + super.getPassword() + "@" + super.getDatabaseUrl());

		if(!databaseExists())
		{
			createDatabase();
			final HikariDataSource hikariDataSource = this.dataSource.unwrap(HikariDataSource.class);
			hikariDataSource.close();
			this.dataSource = sqlService.getDataSource("jdbc:h2://" + super.getUsername() + ":" + super.getPassword() + "@" + super.getDatabaseUrl() + super.getDatabaseName());
		}
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
