package io.github.aquerr.eaglefactions.storage.sql.mysql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;

public class MySQLPlayerStorage extends AbstractPlayerStorage
{
    public MySQLPlayerStorage(final EagleFactions eagleFactions)
    {
        super(eagleFactions, MySQLProvider.getInstance(eagleFactions));
    }
}
