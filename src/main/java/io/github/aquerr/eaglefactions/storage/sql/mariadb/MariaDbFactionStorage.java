package io.github.aquerr.eaglefactions.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;

public class MariaDbFactionStorage extends AbstractFactionStorage
{
	public MariaDbFactionStorage(final EagleFactions plugin)
	{
		super(plugin, MariaDbProvider.getInstance(plugin), new FactionProtectionFlagsMariaDbStorageImpl());
	}
}
