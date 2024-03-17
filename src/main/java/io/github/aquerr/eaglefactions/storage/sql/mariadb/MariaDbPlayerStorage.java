package io.github.aquerr.eaglefactions.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;

public class MariaDbPlayerStorage extends AbstractPlayerStorage
{
	public MariaDbPlayerStorage(final SQLConnectionProvider connectionProvider)
	{
		super(connectionProvider);
	}
}
