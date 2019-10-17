package io.github.aquerr.eaglefactions.common.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ConfigFields;
import io.github.aquerr.eaglefactions.common.storage.sql.SQLProvider;

import java.sql.*;

public class MariaDbProvider implements SQLProvider
{
	private static MariaDbProvider INSTANCE = null;

	private final String databaseUrl;
	private final String databaseName;
	private final String username;
	private final String password;

	public static MariaDbProvider getInstance(final EagleFactions eagleFactions)
	{
		if (INSTANCE == null)
		{
			try
			{
				INSTANCE = new MariaDbProvider(eagleFactions);
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
		return DriverManager.getConnection("jdbc:mariadb://" + this.databaseUrl + this.databaseName, this.username, this.password);
	}

	@Override
	public String getProviderName()
	{
		return "mariadb";
	}

	private MariaDbProvider(final EagleFactions eagleFactions) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException
	{
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
		Connection connection = DriverManager.getConnection("jdbc:mariadb://" + this.databaseUrl + "?user=" + this.username + "&password=" + this.password);
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
		Connection connection = DriverManager.getConnection("jdbc:mariadb://" + this.databaseUrl + "?user=" + this.username + "&password=" + this.password);
		Statement statement = connection.createStatement();
		statement.execute("CREATE SCHEMA " + this.databaseName + ";");
		statement.close();
		connection.close();
	}
}
