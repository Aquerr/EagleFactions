package io.github.aquerr.eaglefactions.storage.sql.mysql;

import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;

public class MySQLPlayerStorage extends AbstractPlayerStorage
{
    public MySQLPlayerStorage(final SQLConnectionProvider connectionProvider)
    {
        super(connectionProvider);
    }
}
