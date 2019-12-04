package io.github.aquerr.eaglefactions.common.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractFactionStorage;

public class MariaDbFactionStorage extends AbstractFactionStorage
{
	public MariaDbFactionStorage(final EagleFactions plugin)
	{
		super(plugin, MariaDbProvider.getInstance(plugin));
	}
}
