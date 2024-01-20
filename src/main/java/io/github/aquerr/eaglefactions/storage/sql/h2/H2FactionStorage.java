package io.github.aquerr.eaglefactions.storage.sql.h2;

import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.FactionChestSqlHelper;
import io.github.aquerr.eaglefactions.storage.sql.FactionProtectionFlagsStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;
import org.apache.logging.log4j.Logger;

public class H2FactionStorage extends AbstractFactionStorage
{
    public H2FactionStorage(final Logger logger,
                            final H2ConnectionProvider connectionProvider)
    {
        super(
                logger,
                connectionProvider,
                new FactionProtectionFlagsH2StorageImpl(),
                new FactionChestSqlHelper());
    }

    public H2FactionStorage(Logger logger,
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
