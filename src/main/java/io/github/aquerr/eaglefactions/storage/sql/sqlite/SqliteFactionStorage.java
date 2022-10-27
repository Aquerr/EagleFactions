package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;

public class SqliteFactionStorage extends AbstractFactionStorage
{
    public SqliteFactionStorage(EagleFactions plugin)
    {
        super(plugin, SqliteProvider.getInstance(plugin), new FactionProtectionFlagsSqliteStorageImpl(SqliteProvider.getInstance(plugin)));
    }
}
