package io.github.aquerr.eaglefactions.common.storage.sql.mysql;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractPlayerStorage;

public class MySQLPlayerStorage extends AbstractPlayerStorage
{
    public MySQLPlayerStorage(final EagleFactionsPlugin eagleFactions)
    {
        super(eagleFactions, MySQLProvider.getInstance(eagleFactions));
    }
}
