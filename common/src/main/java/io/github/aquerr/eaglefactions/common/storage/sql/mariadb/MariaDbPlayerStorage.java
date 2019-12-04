package io.github.aquerr.eaglefactions.common.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractPlayerStorage;

public class MariaDbPlayerStorage extends AbstractPlayerStorage
{
	public MariaDbPlayerStorage(final EagleFactions plugin)
	{
		super(plugin, MariaDbProvider.getInstance(plugin));
	}
}
