package io.github.aquerr.eaglefactions.storage.sql;

public abstract class SQLAbstractConnectionProvider implements SQLConnectionProvider
{
	private final String databaseUrl;
	private final String databaseName;
	private final String username;
	private final String password;

	public SQLAbstractConnectionProvider(DatabaseProperties properties)
	{
		this.databaseUrl = properties.getDatabaseUrl();
		this.databaseName = properties.getDatabaseName();
		this.username = properties.getUsername();
		this.password = properties.getPassword();
	}

	protected String getDatabaseUrl()
	{
		return this.databaseUrl;
	}

	@Override
	public String getDatabaseName()
	{
		return this.databaseName;
	}

	protected String getUsername()
	{
		return this.username;
	}

	protected String getPassword()
	{
		return this.password;
	}
}
