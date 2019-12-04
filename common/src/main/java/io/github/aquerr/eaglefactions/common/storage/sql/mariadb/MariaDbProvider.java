package io.github.aquerr.eaglefactions.common.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLAbstractProvider;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLProvider;

import java.sql.*;

public class MariaDbProvider extends SQLAbstractProvider implements SQLProvider
{
	private static MariaDbProvider INSTANCE = null;

	public static MariaDbProvider getInstance(final EagleFactions eagleFactions)
	{
		if (INSTANCE == null)
		{
			try
			{
				INSTANCE = new MariaDbProvider(eagleFactions);
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
		return DriverManager.getConnection("jdbc:mariadb://" + super.getDatabaseUrl() + super.getDatabaseName(), super.getUsername(), super.getPassword());
	}

	@Override
	public String getProviderName()
	{
		return "mariadb";
	}

	private MariaDbProvider(final EagleFactions eagleFactions) throws SQLException
	{
		super(eagleFactions);
		if(!databaseExists())
			createDatabase();
	}

	private boolean databaseExists() throws SQLException
	{
		//Connection connection = DriverManager.getConnection("jdbc:mysql://" + this.username + ":" + this.password + "@" + this.databaseUrl + this.databaseName);
		final Connection connection = DriverManager.getConnection("jdbc:mariadb://" + super.getDatabaseUrl(), super.getUsername(), super.getPassword());
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
		final Connection connection = DriverManager.getConnection("jdbc:mariadb://" + super.getDatabaseUrl() + "?user=" + super.getUsername() + "&password=" + super.getPassword());
		final Statement statement = connection.createStatement();
		statement.execute("CREATE SCHEMA " + super.getDatabaseName() + ";");
		statement.close();
		connection.close();
	}
}
