package io.github.aquerr.eaglefactions.common.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.StorageConfig;

public class StorageConfigImpl implements StorageConfig
{
	private final Configuration configuration;

	//Storage
	private String storageType = "hocon";
	private String storageUserName = "sa";
	private String storagePassword = "";
	private String databaseUrl = "localhost:3306/";
	private String databaseFileName = "database";

	public StorageConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public void reload()
	{
		this.storageType = configuration.getString("hocon", "storage", "type");
		this.storageUserName = configuration.getString("sa", "storage", "username");
		this.storagePassword = configuration.getString("", "storage", "password");
		this.databaseUrl = configuration.getString("localhost:3306/", "storage", "database-url");
		this.databaseFileName = configuration.getString("database", "storage", "database-file-name");
	}

	@Override
	public String getStorageType()
	{
		return this.storageType;
	}

	@Override
	public String getStorageUsername()
	{
		return this.storageUserName;
	}

	@Override
	public String getStoragePassword()
	{
		return this.storagePassword;
	}

	@Override
	public String getDatabaseUrl()
	{
		return this.databaseUrl;
	}

	@Override
	public String getDatabaseName()
	{
		return this.databaseFileName;
	}
}
