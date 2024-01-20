package io.github.aquerr.eaglefactions.storage.sql.mariadb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.DatabaseProperties;
import io.github.aquerr.eaglefactions.storage.sql.SQLAbstractConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDbConnectionProvider extends SQLAbstractConnectionProvider
{
	private HikariDataSource dataSource;

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

	@Override
	public void close()
	{
		this.dataSource.close();
	}

	public MariaDbConnectionProvider(final DatabaseProperties properties)
	{
		super(properties);

		prepareHikariDataSource();
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
}
