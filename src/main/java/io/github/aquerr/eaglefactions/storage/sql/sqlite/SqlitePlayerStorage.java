package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;

public class SqlitePlayerStorage extends AbstractPlayerStorage
{
    public SqlitePlayerStorage(final EagleFactions plugin)
    {
        super(plugin, SqliteProvider.getInstance(plugin));
    }
}
