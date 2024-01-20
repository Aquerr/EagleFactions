package io.github.aquerr.eaglefactions.storage.sql.mysql;

import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.FactionChestSqlHelper;
import io.github.aquerr.eaglefactions.storage.sql.FactionProtectionFlagsStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;
import org.apache.logging.log4j.Logger;

public class MySQLFactionStorage extends AbstractFactionStorage
{
    public MySQLFactionStorage(Logger logger, MySQLConnectionProvider mySQLConnectionProvider)
    {
        super(
                logger,
                mySQLConnectionProvider,
                new FactionProtectionFlagsMySqlStorageImpl(),
                new FactionChestSqlHelper());
    }

    public MySQLFactionStorage(Logger logger,
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
