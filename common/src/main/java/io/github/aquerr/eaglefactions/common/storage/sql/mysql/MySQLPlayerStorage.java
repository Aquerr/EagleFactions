package io.github.aquerr.eaglefactions.common.storage.sql.mysql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractPlayerStorage;

public class MySQLPlayerStorage extends AbstractPlayerStorage
{
    public MySQLPlayerStorage(final EagleFactions eagleFactions)
    {
        super(eagleFactions, MySQLProvider.getInstance(eagleFactions));
    }
}
