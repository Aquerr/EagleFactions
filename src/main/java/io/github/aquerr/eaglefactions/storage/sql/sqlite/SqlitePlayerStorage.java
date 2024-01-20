package io.github.aquerr.eaglefactions.storage.sql.sqlite;

import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;

public class SqlitePlayerStorage extends AbstractPlayerStorage
{
    public SqlitePlayerStorage(final SqliteConnectionProvider sqliteConnectionProvider)
    {
        super(sqliteConnectionProvider);
    }
}
