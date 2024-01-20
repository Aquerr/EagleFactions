package io.github.aquerr.eaglefactions.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;

public class MariaDbPlayerStorage extends AbstractPlayerStorage
{
	public MariaDbPlayerStorage(final MariaDbConnectionProvider mariaDbConnectionProvider)
	{
		super(mariaDbConnectionProvider);
	}
}
