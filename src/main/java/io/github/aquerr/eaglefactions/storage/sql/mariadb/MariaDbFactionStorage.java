package io.github.aquerr.eaglefactions.storage.sql.mariadb;

import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.FactionChestSqlHelper;
import io.github.aquerr.eaglefactions.storage.sql.FactionProtectionFlagsStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;
import org.apache.logging.log4j.Logger;

public class MariaDbFactionStorage extends AbstractFactionStorage
{
	public MariaDbFactionStorage(Logger logger, final MariaDbConnectionProvider mariaDbConnectionProvider)
	{
		super(
				logger,
				mariaDbConnectionProvider,
				new FactionProtectionFlagsMariaDbStorageImpl(),
				new FactionChestSqlHelper());
	}

	public MariaDbFactionStorage(Logger logger,
							SQLConnectionProvider sqlConnectionProvider,
							FactionProtectionFlagsStorage factionProtectionFlagsStorage,
							FactionChestSqlHelper factionChestSqlHelper)
	{
		super(
				logger,
				sqlConnectionProvider,
				factionProtectionFlagsStorage,
				factionChestSqlHelper);
	}
}
