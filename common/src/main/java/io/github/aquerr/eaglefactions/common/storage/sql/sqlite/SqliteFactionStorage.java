package io.github.aquerr.eaglefactions.common.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractFactionStorage;

public class SqliteFactionStorage extends AbstractFactionStorage
{
    public SqliteFactionStorage(EagleFactions plugin)
    {
        super(plugin, SqliteProvider.getInstance(plugin));
    }
}
