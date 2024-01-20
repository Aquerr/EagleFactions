package io.github.aquerr.eaglefactions.storage.sql.h2;

import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;

public class H2PlayerStorage extends AbstractPlayerStorage
{
    public H2PlayerStorage(H2ConnectionProvider h2ConnectionProvider)
    {
        super(h2ConnectionProvider);
    }
}
