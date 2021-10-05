package io.github.aquerr.eaglefactions.common.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractPlayerStorage;

public class SqlitePlayerStorage extends AbstractPlayerStorage
{
    public SqlitePlayerStorage(final EagleFactions plugin)
    {
        super(plugin, SqliteProvider.getInstance(plugin));
    }
}
