package io.github.aquerr.eaglefactions.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;

public class MariaDbPlayerStorage extends AbstractPlayerStorage
{
	public MariaDbPlayerStorage(final EagleFactions plugin)
	{
		super(plugin, MariaDbProvider.getInstance(plugin));
	}
}
