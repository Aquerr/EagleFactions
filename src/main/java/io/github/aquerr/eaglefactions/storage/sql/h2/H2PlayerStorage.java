package io.github.aquerr.eaglefactions.storage.sql.h2;

import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLConnectionProvider;

public class H2PlayerStorage extends AbstractPlayerStorage
{
    public H2PlayerStorage(SQLConnectionProvider connectionProvider)
    {
        super(connectionProvider);
    }
}
