package io.github.aquerr.eaglefactions.storage.sql.mariadb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.SQLAbstractProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MariaDbProvider extends SQLAbstractProvider
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
				EagleFactionsPlugin.getPlugin().getLogger().error("Error Code: " + e.getErrorCode() + " | SQL State: " + e.getSQLState() + " | Error Message: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
		return INSTANCE;
	}

	public Connection getConnection() throws SQLException
	{
		Connection connection = this.dataSource.getConnection();
		connection.setCatalog(getDatabaseName());
		return connection;
	}

	@Override
	public StorageType getStorageType()
	{
		return StorageType.MARIADB;
	}

	private MariaDbProvider(final EagleFactions eagleFactions) throws SQLException
	{
		super(eagleFactions);

		prepareHikariDataSource();

		if(!databaseExists())
		{
			createDatabase();
		}
	}

	private void prepareHikariDataSource()
	{
		String jdbcUrl = "jdbc:mariadb:" + super.getDatabaseUrl();
		HikariConfig config = new HikariConfig();
		config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
		config.addDataSourceProperty("url", jdbcUrl);
		config.setUsername(super.getUsername());
		config.setPassword(super.getPassword());
		config.setPoolName("eaglefactions");
		config.setMaximumPoolSize(2);
		this.dataSource = new HikariDataSource(config);
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
