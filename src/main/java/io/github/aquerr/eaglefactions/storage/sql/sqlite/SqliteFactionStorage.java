package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.FactionChestSqlHelper;
import io.github.aquerr.eaglefactions.storage.sql.FactionProtectionFlagsStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;
import org.apache.logging.log4j.Logger;

public class SqliteFactionStorage extends AbstractFactionStorage
{
    public SqliteFactionStorage(Logger logger, SqliteConnectionProvider sqliteConnectionProvider)
    {
        super(
                logger,
                sqliteConnectionProvider,
                new FactionProtectionFlagsSqliteStorageImpl(),
                new FactionChestSqlHelper());
    }

    public SqliteFactionStorage(Logger logger,
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
