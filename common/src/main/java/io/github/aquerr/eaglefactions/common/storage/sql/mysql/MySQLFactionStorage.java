package io.github.aquerr.eaglefactions.common.storage.sql.mysql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractFactionStorage;

public class MySQLFactionStorage extends AbstractFactionStorage
{
    public MySQLFactionStorage(final EagleFactions plugin)
    {
        super(plugin, MySQLProvider.getInstance(plugin));
    }
}
