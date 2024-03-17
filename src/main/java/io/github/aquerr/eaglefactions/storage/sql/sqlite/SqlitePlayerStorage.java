package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;

public class SqlitePlayerStorage extends AbstractPlayerStorage
{
    public SqlitePlayerStorage(final SQLConnectionProvider connectionProvider)
    {
        super(connectionProvider);
    }
}
